package com.cagong.receiptpowerserver.domain.mileage.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class TotalMileageResponse {
    private String username;
    private int totalMileagePoints;
    private List<CafeMileageDto> cafeMileageList;
}
