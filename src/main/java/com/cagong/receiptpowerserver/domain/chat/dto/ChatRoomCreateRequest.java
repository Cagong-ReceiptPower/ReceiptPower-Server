package com.cagong.receiptpowerserver.domain.chat.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatRoomCreateRequest {

    @NotBlank
    @Size(max = 255)
    private String title;

    @Min(2)
    @Max(100) // 정책에 맞춰 조정
    private Integer maxParticipants; // 옵션: null 이면 기본값(엔티티에서 10)

}
