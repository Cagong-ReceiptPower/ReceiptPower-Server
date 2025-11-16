package com.cagong.receiptpowerserver.domain.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // [!!] ✅ 2번 (로그 조회) 기능을 위한 핵심 쿼리 메서드
    // 특정 채팅방의 메시지를 생성 시간(createdAt) 오름차순으로 모두 조회
    List<ChatMessage> findByChatRoom_IdOrderByCreatedAtAsc(Long chatRoomId);
}