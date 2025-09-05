package com.cagong.receiptpowerserver.domain.mileage.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class CafeMileageResponse {
    private Long cafeId;
    private String cafeName;
    private int points;
    private LocalDateTime lastUpdatedAt;
}
