package com.cagong.receiptpowerserver.domain.mileage.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class CafeMileageDto {
    private Long cafeId;
    private String cafeName;
    private Long totalPoints;

    public CafeMileageDto(Long cafeId, String cafeName, Long totalPoints) {
        this.cafeId = cafeId;
        this.cafeName = cafeName;
        this.totalPoints = totalPoints;
    }
}
