package com.cagong.receiptpowerserver.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomCreateRequest {

    @NotNull(message = "대상 회원의 ID는 필수입니다")
    private Long targetMemberId;
}