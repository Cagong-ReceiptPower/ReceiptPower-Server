package com.cagong.receiptpowerserver.domain.mileage.dto;

import lombok.Getter;

@Getter
public class SaveMileageRequest {
    private Long cafeId;
    private int remainingTime;
}
