// domain/chat/ChatRoomService.java

package com.cagong.receiptpowerserver.domain.chat;

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
@Transactional(readOnly = true)
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;

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