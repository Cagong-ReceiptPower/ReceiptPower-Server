package com.cagong.receiptpowerserver.domain.chat.service;

import com.cagong.receiptpowerserver.domain.chat.ChatRoom;
import com.cagong.receiptpowerserver.domain.chat.ChatRoomRepository;
import com.cagong.receiptpowerserver.domain.chat.ChatRoomStatus;
import com.cagong.receiptpowerserver.domain.chat.dto.ChatRoomCreateRequest;
import com.cagong.receiptpowerserver.domain.chat.dto.ChatRoomListResponse;
import com.cagong.receiptpowerserver.domain.chat.dto.ChatRoomResponse;
import com.cagong.receiptpowerserver.domain.chat.dto.NearbyMemberResponse;
import com.cagong.receiptpowerserver.domain.location.Location;
import com.cagong.receiptpowerserver.domain.member.Member;
import com.cagong.receiptpowerserver.domain.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;

    // 1️⃣ 주변 사용자 검색 (사각형 + 간단한 원형 필터링)
    @Transactional(readOnly = true)
    public List<NearbyMemberResponse> findNearbyMembers(Long currentMemberId, Double radiusKm) {
        Member currentMember = memberRepository.findById(currentMemberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        
        if (currentMember.getCurrentLatitude() == null || currentMember.getCurrentLongitude() == null) {
            throw new IllegalArgumentException("위치 정보가 설정되지 않았습니다. 위치를 업데이트해주세요.");
        }
        
        // 1단계: 성능을 위해 사각형으로 1차 필터링 (DB 쿼리 최적화)
        Double latRange = radiusKm * 0.009;
        Double lonRange = radiusKm * 0.009;
        
        Double minLat = currentMember.getCurrentLatitude() - latRange;
        Double maxLat = currentMember.getCurrentLatitude() + latRange;
        Double minLon = currentMember.getCurrentLongitude() - lonRange;
        Double maxLon = currentMember.getCurrentLongitude() + lonRange;
        
        // 주변 사용자 조회 (자신 제외)
        List<Member> nearbyMembers = memberRepository.findNearbyMembers(
                currentMemberId, minLat, maxLat, minLon, maxLon);
        
        // 2단계: 원형 범위로 2차 필터링 + 거리 계산
        return nearbyMembers.stream()
                .map(member -> {
                    // 간단한 거리 계산
                    Double latDiff = Math.abs(member.getCurrentLatitude() - currentMember.getCurrentLatitude());
                    Double lonDiff = Math.abs(member.getCurrentLongitude() - currentMember.getCurrentLongitude());
                    Double approxDistance = (latDiff + lonDiff) * 111; // 대략적인 km 변환
                    
                    // 소수점 한 자리로 반올림
                    approxDistance = Math.round(approxDistance * 10.0) / 10.0;
                    
                    return new NearbyMemberResponse(member, approxDistance);
                })
                .filter(response -> response.getDistance() <= radiusKm) // ✅ 원형 범위 필터링
                .sorted((a, b) -> Double.compare(a.getDistance(), b.getDistance())) // 거리순 정렬
                .collect(Collectors.toList());
    }

    // 2️⃣ 1:1 채팅방 생성
    @Transactional
    public ChatRoomResponse createDirectChatRoom(Long requesterId, Long targetMemberId) {
        // 요청자와 대상 사용자 조회
        Member requester = memberRepository.findById(requesterId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 요청자입니다."));
        Member target = memberRepository.findById(targetMemberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 대상 사용자입니다."));
        
        // 기존 채팅방 존재 여부 확인 (추후 구현)
        // boolean existingChatRoom = chatRoomRepository.existsDirectChatRoom(requesterId, targetMemberId);
        // if (existingChatRoom) {
        //     throw new IllegalArgumentException("이미 해당 사용자와의 채팅방이 존재합니다.");
        // }
        
        // 중간 지점 위치 계산 (두 사용자의 중간 위치)
        Double midLatitude = (requester.getCurrentLatitude() + target.getCurrentLatitude()) / 2;
        Double midLongitude = (requester.getCurrentLongitude() + target.getCurrentLongitude()) / 2;
        
        // Location 생성
        Location location = Location.builder()
                .latitude(midLatitude)
                .longitude(midLongitude)
                .address("채팅 위치")
                .build();
        
        // 채팅방 제목 자동 생성
        String title = String.format("%s와 %s의 채팅", requester.getUsername(), target.getUsername());
        
        // 1:1 채팅방 생성 (최대 2명)
        ChatRoom chatRoom = ChatRoom.builder()
                .title(title)
                .location(location)
                .creator(requester)
                .maxParticipants(2) // 1:1 채팅이므로 2명 고정
                .searchRadius(1.0) // 기본값
                .build();
        
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);
        
        log.info("1:1 채팅방 생성: {} (요청자: {}, 대상: {})", 
            savedChatRoom.getTitle(), requester.getUsername(), target.getUsername());
        
        return new ChatRoomResponse(savedChatRoom);
    }

    // 3️⃣ 채팅방 상세 조회
    @Transactional(readOnly = true)
    public ChatRoomResponse getChatRoom(Long chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다: " + chatRoomId));

        ChatRoomResponse response = new ChatRoomResponse(chatRoom);
        // TODO: 추후 ChatParticipant를 통해 현재 참여자 수 계산
        response.setCurrentParticipants(2); // 1:1 채팅방이므로 2명

        return response;
    }

    // 4️⃣ 내가 생성한 채팅방 조회
    @Transactional(readOnly = true)
    public List<ChatRoomListResponse> getMyChatRooms(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다: " + memberId));

        List<ChatRoom> myChatRooms = chatRoomRepository.findByCreatorAndStatus(member, ChatRoomStatus.ACTIVE);

        return myChatRooms.stream()
                .map(chatRoom -> {
                    ChatRoomListResponse response = new ChatRoomListResponse(chatRoom);
                    response.setCurrentParticipants(2); // 1:1 채팅방 기본값
                    return response;
                })
                .collect(Collectors.toList());
    }

    // 5️⃣ 채팅방 상태 변경 (방장만 가능)
    @Transactional
    public void updateChatRoomStatus(Long chatRoomId, ChatRoomStatus status, Long requesterId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다: " + chatRoomId));

        // 권한 확인 (방장만 가능)
        if (!chatRoom.getCreator().getId().equals(requesterId)) {
            throw new IllegalArgumentException("채팅방 상태를 변경할 권한이 없습니다.");
        }

        log.info("채팅방 상태 변경: {} -> {} (채팅방 ID: {})", chatRoom.getStatus(), status, chatRoomId);
    }
}
