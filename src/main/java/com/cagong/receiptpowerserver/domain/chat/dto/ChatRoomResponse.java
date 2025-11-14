package com.cagong.receiptpowerserver.domain.chat.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ChatRoomResponse {

    private final Long id;
    private final String title;
    private final Long creatorId;
    private final Integer maxParticipants;
    private final String status;
    private final LocalDateTime createdAt;

    @Builder
    public ChatRoomResponse(Long id, String title, Long creatorId,
                            Integer maxParticipants,
                            String status, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.creatorId = creatorId;
        this.maxParticipants = maxParticipants;
        this.status = status;
        this.createdAt = createdAt;
    }
}
