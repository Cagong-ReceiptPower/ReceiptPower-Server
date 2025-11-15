package com.cagong.receiptpowerserver.domain.chat.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ChatParticipantCountResponse {

    private final Boolean success;
    private final Long currentParticipants; // 요청서의 currentParticipants

    @Builder
    public ChatParticipantCountResponse(Boolean success, Long currentParticipants) {
        this.success = success;
        this.currentParticipants = currentParticipants;
    }
}