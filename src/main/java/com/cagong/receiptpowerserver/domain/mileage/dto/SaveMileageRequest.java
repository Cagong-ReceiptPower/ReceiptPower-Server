package com.cagong.receiptpowerserver.domain.mileage.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SaveMileageRequest {
    private Long cafeId;
    private int remainingTime;
}
