package com.cagong.receiptpowerserver.domain.mileage.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UseMileageResponse {
    private int availableMileages;
    private LocalDateTime useStartAt;
}
