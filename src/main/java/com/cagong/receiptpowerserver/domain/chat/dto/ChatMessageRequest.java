// chat/dto/ChatMessageRequest.java

package com.cagong.receiptpowerserver.domain.chat.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChatMessageRequest {

    public enum MessageType {
        ENTER, TALK, QUIT
    }

    private MessageType type; // 메시지 타입 (입장, 대화, 퇴장)
    private Long roomId;      // 채팅방 ID
    private String sender; //  채팅 참여자 이름
    private String message;   // 메시지 내용
}