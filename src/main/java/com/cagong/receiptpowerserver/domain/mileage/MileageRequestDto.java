package com.cagong.receiptpowerserver.domain.mileage;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MileageRequestDto {
    private Long memberId;
    private int point;

    // 필요 시 생성자, 빌더 추가
}
