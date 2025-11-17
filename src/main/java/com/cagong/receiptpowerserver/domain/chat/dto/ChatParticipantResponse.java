package com.cagong.receiptpowerserver.domain.chat.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ChatParticipantResponse {
    private final Long userId;
    private final String username;

    @Builder
    public ChatParticipantResponse(Long userId, String username) {
        this.userId = userId;
        this.username = username;
    }
}