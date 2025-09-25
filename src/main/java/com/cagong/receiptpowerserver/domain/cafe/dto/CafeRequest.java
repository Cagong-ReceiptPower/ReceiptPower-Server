package com.cagong.receiptpowerserver.domain.cafe.dto;

import lombok.Getter;

@Getter
public class CafeRequest {
    private String cafeName;
    private String address;
    private double latitude;
    private double longitude;
    private String phoneNumber;
}
