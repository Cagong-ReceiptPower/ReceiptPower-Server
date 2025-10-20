package com.cagong.receiptpowerserver.domain.chat.dto;

import com.cagong.receiptpowerserver.domain.chat.ChatRoomStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatRoomStatusUpdateRequest {
    @NotNull
    private ChatRoomStatus status;
}
