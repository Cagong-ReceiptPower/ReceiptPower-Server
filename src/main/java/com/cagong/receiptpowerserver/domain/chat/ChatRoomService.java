// domain/chat/ChatRoomService.java

package com.cagong.receiptpowerserver.domain.chat;

import com.cagong.receiptpowerserver.domain.chat.dto.*;
import com.cagong.receiptpowerserver.domain.member.Member;
import com.cagong.receiptpowerserver.domain.member.MemberRepository;
import com.cagong.receiptpowerserver.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Transactional
    public ChatRoomResponse create(ChatRoomCreateRequest req, Long authenticatedUserId) {
        Member creator = memberRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new NotFoundException("creator not found: " + authenticatedUserId));

        String title = req.getTitle().trim();
        if (title.isEmpty()) {
            throw new IllegalArgumentException("title must not be blank");
        }

        ChatRoom chatRoom = ChatRoom.builder()
                .title(title)
                .creator(creator)
                .maxParticipants(req.getMaxParticipants())
                .build();

        ChatRoom saved = chatRoomRepository.save(chatRoom);

        return toResponse(saved);
    }

    // 모든 활성화된 채팅방 조회
    public List<ChatRoomResponse> getAllActiveRooms() {
        return chatRoomRepository.findByStatus(ChatRoomStatus.ACTIVE)
                .stream().map(this::toResponse).toList();
    }

    public ChatRoomResponse getById(Long id) {
        ChatRoom room = chatRoomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("chat room not found: " + id));
        return toResponse(room);
    }

    public List<ChatRoomResponse> getMyRooms(Long authenticatedUserId) {
        Member creator = memberRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new NotFoundException("creator not found: " + authenticatedUserId));
        return chatRoomRepository.findByCreatorAndStatus(creator, ChatRoomStatus.ACTIVE)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public ChatRoomResponse updateStatus(Long roomId, Long authenticatedUserId, ChatRoomStatus newStatus) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("chat room not found: " + roomId));

        if (room.getCreator() == null || !room.getCreator().getId().equals(authenticatedUserId)) {
            throw new IllegalStateException("only creator can change the room status");
        }

        room.setStatus(newStatus);
        return toResponse(room);
    }

    /**
     * ✅ 1. 현재 참여 인원 조회 로직
     * (ChatRoomController의 getChatRoomParticipants가 호출)
     */
    public List<ChatParticipantResponse> getParticipants(Long roomId) {
        if (!chatRoomRepository.existsById(roomId)) {
            throw new NotFoundException("chat room not found: " + roomId);
        }

        // ChatParticipantRepository를 사용해 특정 방의 참여자 목록을 조회
        return chatParticipantRepository.findByChatRoom_Id(roomId).stream()
                .map(participant -> ChatParticipantResponse.builder()
                        .userId(participant.getMember().getId())
                        .username(participant.getMember().getUsername()) // 또는 getName() 등
                        .build())
                .toList();
    }

    /**
     * ✅ 2. 인원 4명 제한 로직 (채팅방 입장)
     * (ChatRoomController의 enterChatRoom이 호출)
     */
    @Transactional
    public ChatParticipantCountResponse enterRoom(Long roomId, Long authenticatedUserId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("chat room not found: " + roomId));

        // 입장 전 상태 확인
        if (room.getStatus() != ChatRoomStatus.ACTIVE) { //
            throw new IllegalStateException("활성화된 채팅방이 아닙니다.");
        }

        // [!!] ✅ 2. 인원 제한 로직
        // ChatRoom 엔티티의 maxParticipants 값을 기준으로 현재 인원을 비교
        long currentParticipants = chatParticipantRepository.countByChatRoom_Id(roomId);
        if (currentParticipants >= room.getMaxParticipants()) {
            throw new IllegalStateException("채  팅방 정원이 초과되었습니다.");
        }

        // 이미 참여 중인지 확인
        if (chatParticipantRepository.existsByChatRoom_IdAndMember_Id(roomId, authenticatedUserId)) {
            // 이미 참여중이어도 에러 대신 성공으로 간주하고 현재 인원수 반환
            return ChatParticipantCountResponse.builder()
                    .success(true)
                    .currentParticipants(currentParticipants)
                    .build();
        }

        Member member = memberRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new NotFoundException("member not found: " + authenticatedUserId));

        // 참여자 목록(ChatParticipant)에 저장
        ChatParticipant participation = ChatParticipant.builder().chatRoom(room).member(member).build();
        chatParticipantRepository.save(participation);

        // 입장 성공 시, 현재 인원 + 1
        return ChatParticipantCountResponse.builder()
                .success(true)
                .currentParticipants(currentParticipants + 1)
                .build();
    }

    /**
     * [보너스] 채팅방 나가기 로직
     * (ChatRoomController의 leaveChatRoom이 호출)
     */
    @Transactional
    public ChatParticipantCountResponse leaveRoom(Long roomId, Long authenticatedUserId) {
        ChatParticipant participation = chatParticipantRepository.findByChatRoom_IdAndMember_Id(roomId, authenticatedUserId)
                .orElse(null); // 참여 기록이 없으면 무시

        if (participation != null) {
            chatParticipantRepository.delete(participation);
        }

        long currentParticipants = chatParticipantRepository.countByChatRoom_Id(roomId);
        return ChatParticipantCountResponse.builder()
                .success(true)
                .currentParticipants(currentParticipants)
                .build();
    }

    /**
     * ✅ 3. 채팅방 24시간 만료 처리 로직
     * (ChatRoomScheduler가 이 메서드를 호출)
     */
    @Transactional
    public void closeExpiredRooms() {
        System.out.println("스케줄러 실행: 24시간 만료된 채팅방을 확인합니다...");

        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);

        // 24시간이 지났고, ACTIVE 상태인 방들을 조회 (Repository에 쿼리 메서드 추가 필요)
        List<ChatRoom> expiredRooms = chatRoomRepository.findByStatusAndCreatedAtBefore(
                ChatRoomStatus.ACTIVE, //
                twentyFourHoursAgo
        );

        int count = 0;
        for (ChatRoom room : expiredRooms) {
            room.setStatus(ChatRoomStatus.CLOSED); //
            count++;
        }

        if (count > 0) {
            System.out.printf("%d개의 채팅방을 CLOSED로 변경했습니다.%n", count);
        }
    }

    /**
     * (옵션) 채팅 메시지 로그 조회
     * (ChatRoomController의 getChatRoomMessages가 호출)
     */
    public List<ChatMessageResponse> getMessages(Long roomId) {
        // [!!] ChatMessageRepository 구현이 필요합니다.
        // List<ChatMessage> messages = chatMessageRepository.findByChatRoomId(roomId);
        // return messages.stream().map(DTO로 변환).toList();

        List<ChatMessage> messages = chatMessageRepository.findByChatRoom_IdOrderByCreatedAtAsc(roomId);

        // [!!] 2. ChatMessage(엔티티) -> ChatMessageResponse(DTO)로 변환
        return messages.stream()
                .map(this::toChatMessageResponse) // 헬퍼 메서드 사용
                .toList();
    }

    // --- (이하 기존 toResponse 메서드) ---
    private ChatRoomResponse toResponse(ChatRoom saved) { //
        return ChatRoomResponse.builder()
                .id(saved.getId())
                .title(saved.getTitle())
                .creatorId(saved.getCreator() != null ? saved.getCreator().getId() : null)
                .maxParticipants(saved.getMaxParticipants())
                .status(saved.getStatus().name())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    /**
     * ChatMessage 엔티티를 ChatMessageResponse DTO로 변환
     */
    private ChatMessageResponse toChatMessageResponse(ChatMessage entity) {
        // [!!] sender가 null일 경우를 대비한 방어 코드 (e.g. 탈퇴한 유저)
        Member sender = entity.getSender();
        Long senderId = (sender != null) ? sender.getId() : null;
        String senderName = (sender != null) ? sender.getUsername() : "알 수 없는 사용자";

        return ChatMessageResponse.builder()
                .id(entity.getId())
                .roomId(entity.getChatRoom().getId())
                .senderId(senderId)
                .senderName(senderName)
                .message(entity.getMessage())
                .timestamp(entity.getCreatedAt())
                .build();
    }
}