package com.cagong.receiptpowerserver.domain.chat;

import com.cagong.receiptpowerserver.domain.cafe.Cafe;
import com.cagong.receiptpowerserver.domain.cafe.CafeRepository;
import com.cagong.receiptpowerserver.domain.member.Member;
import com.cagong.receiptpowerserver.domain.member.MemberRepository;
import com.cagong.receiptpowerserver.domain.chat.dto.ChatRoomCreateRequest;
import com.cagong.receiptpowerserver.domain.chat.dto.ChatRoomResponse;
import com.cagong.receiptpowerserver.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;
    private final CafeRepository cafeRepository;

    @Transactional
    public ChatRoomResponse create(ChatRoomCreateRequest req, Long authenticatedUserId) {
        if (authenticatedUserId == null) {
            throw new IllegalStateException("authenticated user id is required");
        }

        Member creator = memberRepository.findById(authenticatedUserId)
            .orElseThrow(() -> new NotFoundException("creator not found: " + authenticatedUserId));

        // [수정 1] cafeId로 Cafe 엔티티를 찾아옵니다.
        Cafe cafe = cafeRepository.findById(req.getCafeId())
                .orElseThrow(() -> new NotFoundException("cafe not found: " + req.getCafeId()));

        String title = req.getTitle() != null ? req.getTitle().trim() : null;
        if (title == null || title.isEmpty()) {
            throw new IllegalArgumentException("title must not be blank");
        }

        ChatRoom chatRoom = ChatRoom.builder()
            .title(title)
            .creator(creator)
            .cafe(cafe)
            .maxParticipants(req.getMaxParticipants()) // null이면 엔티티 빌더에서 기본값 처리
            .build();

        ChatRoom saved = chatRoomRepository.save(chatRoom);

        return toResponse(saved);
    }

    // [추가] 특정 카페에 속한 채팅방 목록을 조회하는 서비스 메서드
    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getRoomsByCafe(Long cafeId) {
        return chatRoomRepository.findByCafeIdAndStatus(cafeId, ChatRoomStatus.ACTIVE)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ChatRoomResponse getById(Long id) {
        ChatRoom room = chatRoomRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("chat room not found: " + id));
        return toResponse(room);
    }

    @Transactional(readOnly = true)
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
        // JPA dirty checking으로 업데이트
        return toResponse(room);
    }

    private ChatRoomResponse toResponse(ChatRoom saved) {
        return ChatRoomResponse.builder()
            .id(saved.getId())
            .title(saved.getTitle())
            .creatorId(saved.getCreator() != null ? saved.getCreator().getId() : null)
            .maxParticipants(saved.getMaxParticipants())
            .status(saved.getStatus().name())
            .createdAt(saved.getCreatedAt())
            .build();
    }
}
