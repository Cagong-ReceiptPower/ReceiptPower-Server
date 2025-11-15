package com.cagong.receiptpowerserver.domain.chat.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ApiErrorResponse {
    private final Boolean success;
    private final String message;

    @Builder
    public ApiErrorResponse(Boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}