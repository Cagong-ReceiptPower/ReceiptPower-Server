package com.cagong.receiptpowerserver.domain.cafe.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CafeRequest {
    private String cafeName;
    private String address;
    private double latitude;
    private double longitude;
    private String phoneNumber;
}
