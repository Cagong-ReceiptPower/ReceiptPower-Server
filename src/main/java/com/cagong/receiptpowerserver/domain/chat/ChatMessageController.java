// chat/ChatMessageController.java

package com.cagong.receiptpowerserver.domain.chat;

import com.cagong.receiptpowerserver.domain.chat.dto.ChatMessageRequest;
import com.cagong.receiptpowerserver.domain.member.Member;
import com.cagong.receiptpowerserver.domain.member.MemberRepository;
import com.cagong.receiptpowerserver.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener; // [!!] 추가
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent; // [!!] 추가

import java.util.Map; // [!!] 추가

@Controller
@RequiredArgsConstructor
public class ChatMessageController {

    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatParticipantRepository chatParticipantRepository;
    // 메시지 저장을 위해서 추가함.
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;

    @MessageMapping("/chat/message")
    public void message(@Payload ChatMessageRequest message, StompHeaderAccessor headerAccessor) {

        // [핵심] STOMP 세션 속성에서 StompHandler가 저장해 둔 사용자 이름을 직접 가져옵니다.
        String senderName = (String) headerAccessor.getSessionAttributes().get("username");
        Long senderId = (Long) headerAccessor.getSessionAttributes().get("userId");

        // [!!] 수정: senderId가 null이어도 TALK 메시지 저장 시 오류가 나므로 함께 체크
        if (senderName == null || senderId == null) {
            // 비정상 접근으로 세션 정보가 없으면 메시지 처리를 중단합니다.
            return;
        }

        message.setSender(senderName);
        Long roomId = message.getRoomId();

        if (ChatMessageRequest.MessageType.ENTER.equals(message.getType())) {
            message.setMessage(senderName + "님이 입장하셨습니다.");
            // [!!] 비정상 종료(disconnect) 시 roomId를 참조하기 위해 세션에 저장
            headerAccessor.getSessionAttributes().put("roomId", roomId);

        } else if (ChatMessageRequest.MessageType.QUIT.equals(message.getType())) {
            message.setMessage(senderName + "님이 퇴장하셨습니다.");

        } else if (ChatMessageRequest.MessageType.TALK.equals(message.getType())) {

            // [!!] --- ✅ 메시지 저장 로직 ---
            // 1. 채팅방 엔티티 조회
            ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                    .orElseThrow(() -> new NotFoundException("Chat room not found: " + roomId));
            // 2. 보낸사람 엔티티 조회
            Member sender = memberRepository.findById(senderId)
                    .orElseThrow(() -> new NotFoundException("Member not found: " + senderId));

            // 3. 메시지 엔티티 생성
            ChatMessage chatMessage = ChatMessage.builder()
                    .chatRoom(chatRoom)
                    .sender(sender)
                    .message(message.getMessage())
                    .build();

            // 4. DB에 저장 (INSERT)
            chatMessageRepository.save(chatMessage);
            // --- 메시지 저장 로직 끝 ---
        }

        // [!!] 수정: 메시지 1회 전송
        // 1. 기존 채팅 메시지(ENTER, TALK, QUIT) 전송
        messagingTemplate.convertAndSend("/sub/chat/room/" + roomId, message);

        // 2. 참여자 수 변동 이벤트 전송 (ENTER, QUIT 일 때만)
        if (ChatMessageRequest.MessageType.ENTER.equals(message.getType()) ||
                ChatMessageRequest.MessageType.QUIT.equals(message.getType())) {

            // [!!] 헬퍼 메서드 호출
            sendParticipantUpdate(roomId);
        }

        // [!!] 수정: 중복 전송 코드 삭제
        // messagingTemplate.convertAndSend("/sub/chat/room/" + message.getRoomId(), message);
    }

    /**
     * [!!] 신규 추가 (✅ 6번: 비정상 종료 처리)
     * WebSocket 연결이 비정상적으로 종료되었을 때 (예: 앱 종료, 네트워크 끊김)
     * QUIT 메시지를 서버가 직접 발생시켜 다른 참여자들에게 알립니다.
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        // 세션에서 ENTER 시 저장해둔 사용자 이름과 방 ID를 가져옵니다.
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        Long roomId = (Long) headerAccessor.getSessionAttributes().get("roomId");

        if (username != null && roomId != null) {
            // 1. QUIT 메시지 생성
            ChatMessageRequest quitMessage = new ChatMessageRequest();
            quitMessage.setType(ChatMessageRequest.MessageType.QUIT);
            quitMessage.setSender(username);
            quitMessage.setRoomId(roomId);
            quitMessage.setMessage(username + "님이 퇴장하셨습니다.");

            // 2. 해당 채팅방에 퇴장 메시지 브로드캐스팅
            messagingTemplate.convertAndSend("/sub/chat/room/" + roomId, quitMessage);

            // 3. 비정상 종료 시에도 인원수 업데이트 이벤트 전송
            sendParticipantUpdate(roomId);
        }
    }

    /**
     * [!!] 신규 추가 (✅ 6번: 참여자 수 업데이트 이벤트)
     * 현재 인원수를 계산하여 PARTICIPANT_UPDATE 이벤트를 전송하는 헬퍼 메서드
     */
    private void sendParticipantUpdate(Long roomId) {
        if (roomId == null) return;

        // ChatParticipantRepository를 사용해 현재 인원수를 DB에서 조회
        long currentParticipants = chatParticipantRepository.countByChatRoom_Id(roomId);

        // 요청하신 JSON 규격대로 Map을 생성하여 전송
        Map<String, Object> updateEvent = Map.of(
                "type", "PARTICIPANT_UPDATE",
                "roomId", roomId,
                "currentParticipants", currentParticipants
        );

        // /sub/chat/room/{roomId} 주소로 인원수 업데이트 이벤트 전송
        messagingTemplate.convertAndSend("/sub/chat/room/" + roomId, updateEvent);
    }
}