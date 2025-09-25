package com.cagong.receiptpowerserver.domain.chat;

import com.cagong.receiptpowerserver.domain.member.Member;
import com.cagong.receiptpowerserver.domain.member.MemberRepository;
import com.cagong.receiptpowerserver.domain.chat.dto.ChatRoomCreateRequest;
import com.cagong.receiptpowerserver.domain.chat.dto.ChatRoomResponse;
import com.cagong.receiptpowerserver.exception.NotFoundException;
import com.cagong.receiptpowerserver.domain.location.Location;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public ChatRoomResponse create(ChatRoomCreateRequest req, Long authenticatedUserId) {
        if (authenticatedUserId == null) {
            throw new IllegalStateException("authenticated user id is required");
        }

        Member creator = memberRepository.findById(authenticatedUserId)
            .orElseThrow(() -> new NotFoundException("creator not found: " + authenticatedUserId));

        String title = req.getTitle() != null ? req.getTitle().trim() : null;
        if (title == null || title.isEmpty()) {
            throw new IllegalArgumentException("title must not be blank");
        }

        // 위치 생성 (Location API는 프로젝트 구현에 맞게 조정)
        Location location = new Location(req.getLatitude(), req.getLongitude());

        ChatRoom chatRoom = ChatRoom.builder()
            .title(title)
            .creator(creator)
            .location(location)
            .maxParticipants(req.getMaxParticipants()) // null이면 엔티티 빌더에서 기본값 처리
            .searchRadius(req.getSearchRadius())       // null이면 엔티티 빌더에서 기본값 처리
            .build();

        ChatRoom saved = chatRoomRepository.save(chatRoom);

        return toResponse(saved);
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

    @Transactional(readOnly = true)
    public List<ChatRoomResponse> findNearby(Double latitude, Double longitude, Double radiusKm) {
        // 단순 경계박스 탐색 (대략적인 거리 계산)
        double latDegree = radiusKm / 111.0; // 위도 1도 ≈ 111km
        double lonDegree = radiusKm / (111.0 * Math.cos(Math.toRadians(latitude)));

        double minLat = latitude - latDegree;
        double maxLat = latitude + latDegree;
        double minLon = longitude - lonDegree;
        double maxLon = longitude + lonDegree;

        return chatRoomRepository
            .findByStatusAndLocationLatitudeBetweenAndLocationLongitudeBetween(
                ChatRoomStatus.ACTIVE, minLat, maxLat, minLon, maxLon
            )
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
            .searchRadius(saved.getSearchRadius())
            .status(saved.getStatus().name())
            .createdAt(saved.getCreatedAt())
            .build();
    }
}
