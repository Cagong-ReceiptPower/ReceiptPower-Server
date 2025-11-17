package com.cagong.receiptpowerserver.domain.chat;

import com.cagong.receiptpowerserver.domain.chat.dto.ChatMessageRequest;
import com.cagong.receiptpowerserver.domain.chat.dto.ChatMessageResponse; // [!!] 응답 DTO 임포트
// [!!] 리포지토리 의존성 모두 제거됨
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * [리팩토링] WebSocket 메시지 중계(Routing) 역할만 담당하는 컨트롤러.
 * 모든 비즈니스 로직은 ChatMessageService에 위임합니다.
 */
@Controller
@RequiredArgsConstructor
public class ChatMessageController {

    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatMessageService chatMessageService; // [!!] 신규 서비스 주입

    // [!!] 모든 Repository 의존성 제거됨

    /**
     * /pub/chat/message 경로로 오는 메시지 처리
     */
    @MessageMapping("/chat/message")
    public void message(@Payload ChatMessageRequest message, StompHeaderAccessor headerAccessor) {

        // 1. 세션에서 사용자 정보 가져오기 (인증 정보)
        String senderName = (String) headerAccessor.getSessionAttributes().get("username");
        Long senderId = (Long) headerAccessor.getSessionAttributes().get("userId");
        Long roomId = message.getRoomId();

        if (senderName == null || senderId == null) {
            return; // 비정상 접근 차단
        }
        message.setSender(senderName); // (ENTER, QUIT 메시지용)

        // 2. TALK 메시지인 경우, 서비스에 저장을 위임
        if (ChatMessageRequest.MessageType.TALK.equals(message.getType())) {

            // 서비스 호출: DB에 메시지 저장 후, 브로드캐스팅용 DTO 반환
            ChatMessageResponse responseDto = chatMessageService.saveMessage(message, senderId, senderName);

            // 클라이언트가 일관된 응답(ChatMessageResponse)을 받도록 DTO로 브로드캐스팅
            messagingTemplate.convertAndSend("/sub/chat/room/" + roomId, responseDto);

        } else {
            // 3. ENTER, QUIT 메시지는 DB 저장 없이 브로드캐스팅
            // (기존 ChatMessageRequest 포맷 그대로 전송)
            if (ChatMessageRequest.MessageType.ENTER.equals(message.getType())) {
                message.setMessage(senderName + "님이 입장하셨습니다.");
                // 비정상 종료 처리를 위해 세션에 roomId 저장
                headerAccessor.getSessionAttributes().put("roomId", roomId);
            } else if (ChatMessageRequest.MessageType.QUIT.equals(message.getType())) {
                message.setMessage(senderName + "님이 퇴장하셨습니다.");
            }

            messagingTemplate.convertAndSend("/sub/chat/room/" + roomId, message);
        }

        // 4. 참여자 수 업데이트 이벤트 전송 (ENTER, QUIT 일 때만)
        if (ChatMessageRequest.MessageType.ENTER.equals(message.getType()) ||
                ChatMessageRequest.MessageType.QUIT.equals(message.getType())) {

            // 서비스의 헬퍼 메서드 호출
            chatMessageService.broadcastParticipantUpdate(roomId);
        }
    }

    /**
     * 비정상 종료(Disconnect) 이벤트 처리
     * (로직을 ChatMessageService에 위임)
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String username = (String) headerAccessor.getSessionAttributes().get("username");
        Long roomId = (Long) headerAccessor.getSessionAttributes().get("roomId");

        // 서비스 메서드 호출
        chatMessageService.handleDisconnect(username, roomId);
    }
}