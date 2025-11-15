// [새 파일] domain/chat/ChatRoomScheduler.java
package com.cagong.receiptpowerserver.domain.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatRoomScheduler {

    private final ChatRoomService chatRoomService;

    /**
     * ✅ 3. 매시간 정각에 만료된 채팅방을 확인
     * (cron = "0 0 * * * *")
     */
    @Scheduled(cron = "0 0 * * * *")
    public void runCloseExpiredRooms() {
        chatRoomService.closeExpiredRooms();
    }
}