package com.cagong.receiptpowerserver.domain.chat;

import com.cagong.receiptpowerserver.domain.chat.dto.ChatMessageRequest;
import com.cagong.receiptpowerserver.domain.chat.dto.ChatMessageResponse;
import com.cagong.receiptpowerserver.domain.member.Member;
import com.cagong.receiptpowerserver.domain.member.MemberRepository;
import com.cagong.receiptpowerserver.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * [신규] 채팅 메시지 관련 모든 비즈니스 로직을 전담하는 서비스
 * (WebSocket, HTTP API, 이벤트 처리 로직 포함)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatMessageService {

    // --- 의존성 주입 ---
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final SimpMessageSendingOperations messagingTemplate;

    /**
     * [WebSocket] 실시간 채팅 메시지 저장 및 브로드캐스팅
     * (ChatMessageController의 TALK 타입 로직 이관)
     *
     * @param request    클라이언트 요청 DTO
     * @param senderId   JWT 토큰에서 추출한 발신자 ID
     * @param senderName JWT 토큰에서 추출한 발신자 이름
     * @return 브로드캐스팅에 사용될 ChatMessageResponse DTO
     */
    @Transactional
    public ChatMessageResponse saveMessage(ChatMessageRequest request, Long senderId, String senderName) {
        // 1. 발신자(Member) 엔티티 조회
        Member sender = memberRepository.findById(senderId)
                .orElseThrow(() -> new NotFoundException("Member not found: " + senderId));

        // 2. 채팅방(ChatRoom) 엔티티 조회
        ChatRoom chatRoom = chatRoomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new NotFoundException("Chat room not found: " + request.getRoomId()));

        // 3. 메시지 엔티티 생성
        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .message(request.getMessage())
                .build();

        // 4. DB에 저장 (INSERT)
        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);

        // 5. 엔티티를 DTO로 변환하여 반환
        return toChatMessageResponse(savedMessage);
    }

    /**
     * [HTTP API] 특정 채팅방의 과거 메시지 목록 조회
     * (ChatRoomService에서 이관)
     *
     * @param roomId 채팅방 ID
     * @return 메시지 DTO 리스트
     */
    public List<ChatMessageResponse> getMessages(Long roomId) {
        if (!chatRoomRepository.existsById(roomId)) {
            throw new NotFoundException("Chat room not found: " + roomId);
        }

        List<ChatMessage> messages = chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(roomId);

        return messages.stream()
                .map(this::toChatMessageResponse)
                .toList();
    }

    /**
     * [WebSocket Event] 비정상 종료 처리
     * (ChatMessageController의 handleWebSocketDisconnectListener 로직 이관)
     *
     * @param username 세션에 저장된 사용자 이름
     * @param roomId   세션에 저장된 방 ID
     */
    public void handleDisconnect(String username, Long roomId) {
        if (username != null && roomId != null) {
            // 1. QUIT 메시지 생성
            ChatMessageRequest quitMessage = new ChatMessageRequest();
            quitMessage.setType(ChatMessageRequest.MessageType.QUIT);
            quitMessage.setSender(username);
            quitMessage.setRoomId(roomId);
            quitMessage.setMessage(username + "님이 퇴장하셨습니다.");

            // 2. 해당 채팅방에 퇴장 메시지 브로드캐스팅
            messagingTemplate.convertAndSend("/sub/chat/room/" + roomId, quitMessage);

            // 3. 비정상 종료 시에도 인원수 업데이트 이벤트 전송
            broadcastParticipantUpdate(roomId);
        }
    }

    /**
     * [Helper] 참여자 수 업데이트 이벤트 브로드캐스팅
     * (ChatMessageController의 sendParticipantUpdate 로직 이관)
     *
     * @param roomId 방 ID
     */
    public void broadcastParticipantUpdate(Long roomId) {
        if (roomId == null) return;

        // DB에서 현재 인원수를 조회
        long currentParticipants = chatParticipantRepository.countByChatRoom_Id(roomId);

        Map<String, Object> updateEvent = Map.of(
                "type", "PARTICIPANT_UPDATE",
                "roomId", roomId,
                "currentParticipants", currentParticipants
        );

        messagingTemplate.convertAndSend("/sub/chat/room/" + roomId, updateEvent);
    }

    /**
     * [Helper] ChatMessage 엔티티를 ChatMessageResponse DTO로 변환
     * (ChatRoomService의 헬퍼 메서드 이관)
     */
    private ChatMessageResponse toChatMessageResponse(ChatMessage entity) {
        Member sender = entity.getSender();
        Long senderId = (sender != null) ? sender.getId() : null;
        String senderName = (sender != null) ? sender.getUsername() : "알 수 없는 사용자"; // getUsername() 또는 getNickname()

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