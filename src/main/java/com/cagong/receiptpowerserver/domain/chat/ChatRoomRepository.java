package com.cagong.receiptpowerserver.domain.chat;

import com.cagong.receiptpowerserver.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    // status로 채팅방 목록 조회
    List<ChatRoom> findByStatus(ChatRoomStatus status);

    // 생성자별 채팅방 조회
    List<ChatRoom> findByCreatorAndStatus(Member creator, ChatRoomStatus status);

    List<ChatRoom> findByStatusAndCreatedAtBefore(ChatRoomStatus status, LocalDateTime dateTime);
}
