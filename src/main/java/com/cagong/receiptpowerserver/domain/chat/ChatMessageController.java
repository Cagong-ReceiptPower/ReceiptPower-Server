// chat/ChatMessageController.java

package com.cagong.receiptpowerserver.domain.chat;

import com.cagong.receiptpowerserver.domain.chat.dto.ChatMessageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatMessageController {

    private final SimpMessageSendingOperations messagingTemplate;

    @MessageMapping("/chat/message")
    public void message(@Payload ChatMessageRequest message, StompHeaderAccessor headerAccessor) {

        // [핵심] STOMP 세션 속성에서 StompHandler가 저장해 둔 사용자 이름을 직접 가져옵니다.
        String senderName = (String) headerAccessor.getSessionAttributes().get("username");

        // 만약 비정상적인 접근으로 senderName이 없다면, 메시지 처리를 중단합니다.
        if (senderName == null) {
            return;
        }

        message.setSender(senderName);

        if (ChatMessageRequest.MessageType.ENTER.equals(message.getType())) {
            message.setMessage(senderName + "님이 입장하셨습니다.");
        }

        messagingTemplate.convertAndSend("/sub/chat/room/" + message.getRoomId(), message);
    }
}