package com.cagong.receiptpowerserver.domain.chat;

import com.cagong.receiptpowerserver.domain.chat.dto.ChatMessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChattingController {

    // 클라이언트로부터 메시지를 받고, 다른 클라이언트에게 메시지를 전달하는 메서드
    @MessageMapping("/messages") // 클라이언트가 메시지를 보낼 경로: /pub/messages
    @SendTo("/sub/chat")      // 메시지를 전달할 경로: /sub/chat
    public ChatMessageDto sendMessage(ChatMessageDto message) {
        // 여기에서 메시지를 처리하고 데이터베이스에 저장하는 로직을 추가할 수 있습니다.
        // 현재는 받은 메시지를 그대로 다시 반환하여 모든 클라이언트에게 전달합니다.
        return message;
    }
}