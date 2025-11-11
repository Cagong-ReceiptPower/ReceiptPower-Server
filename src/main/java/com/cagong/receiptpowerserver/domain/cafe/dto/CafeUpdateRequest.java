package com.cagong.receiptpowerserver.domain.cafe.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CafeUpdateRequest {

    private String cafeName;
    private String address;
    private Double latitude;
    private Double longitude;
    private String phoneNumber;
}
