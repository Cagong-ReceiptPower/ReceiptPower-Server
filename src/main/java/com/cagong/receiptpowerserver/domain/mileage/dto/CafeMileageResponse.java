package com.cagong.receiptpowerserver.domain.mileage.dto;

import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
public class CafeMileageResponse {
    private Long cafeId;
    private String cafeName;
    private int points;
    private LocalDateTime lastUpdatedAt;
}
