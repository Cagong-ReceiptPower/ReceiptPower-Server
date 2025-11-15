package com.cagong.receiptpowerserver.domain.chat.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ChatMessageResponse {

    private final Long id;
    private final Long roomId;
    private final Long senderId;
    private final String senderName;
    private final String message;
    private final LocalDateTime timestamp; // 요청서의 timestamp

    @Builder
    public ChatMessageResponse(Long id, Long roomId, Long senderId, String senderName, String message, LocalDateTime timestamp) {
        this.id = id;
        this.roomId = roomId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.message = message;
        this.timestamp = timestamp;
    }
}