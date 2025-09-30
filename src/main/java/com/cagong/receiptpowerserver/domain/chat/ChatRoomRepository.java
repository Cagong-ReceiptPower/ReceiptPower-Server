package com.cagong.receiptpowerserver.domain.chat;

import com.cagong.receiptpowerserver.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    // [추가] cafeId로 활성화된 채팅방 목록을 찾는 메서드
    List<ChatRoom> findByCafeIdAndStatus(Long cafeId, ChatRoomStatus status);

    // 생성자별 채팅방 조회
    List<ChatRoom> findByCreatorAndStatus(Member creator, ChatRoomStatus status);
}
