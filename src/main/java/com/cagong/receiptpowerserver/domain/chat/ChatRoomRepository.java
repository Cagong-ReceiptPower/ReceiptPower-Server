package com.cagong.receiptpowerserver.domain.chat;

import com.cagong.receiptpowerserver.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    // 위치 기반 검색
    List<ChatRoom> findByStatusAndLocationLatitudeBetweenAndLocationLongitudeBetween(
        ChatRoomStatus status, Double minLat, Double maxLat, Double minLon, Double maxLon);
    
    // 생성자별 채팅방 조회
    List<ChatRoom> findByCreatorAndStatus(Member creator, ChatRoomStatus status);
}
