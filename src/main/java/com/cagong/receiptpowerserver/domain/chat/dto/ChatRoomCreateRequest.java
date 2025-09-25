package com.cagong.receiptpowerserver.domain.chat.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
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

    // creatorId는 인증 정보에서만 사용하도록 제거되었습니다.

    @Min(2)
    @Max(100) // 정책에 맞춰 조정
    private Integer maxParticipants; // 옵션: null 이면 기본값(엔티티에서 10)

    @DecimalMin(value = "1.0")
    @DecimalMax(value = "50.0") // 정책에 맞춰 조정 (예: 최대 50km)
    private Double searchRadius; // 옵션: null 이면 기본값(엔티티에서 1.0)

    @NotNull
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private Double latitude;

    @NotNull
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private Double longitude;
}
