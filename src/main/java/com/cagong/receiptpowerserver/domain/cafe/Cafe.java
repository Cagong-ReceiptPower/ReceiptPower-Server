package com.cagong.receiptpowerserver.domain.cafe;

import com.cagong.receiptpowerserver.domain.cafe.dto.CafeRequest;
import com.cagong.receiptpowerserver.domain.cafe.dto.CafeUpdateRequest;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cafe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cafe_id")
    private Long id;

    // [추가] 카카오 장소 ID를 저장할 필드. 카페가 중복 저장되는 것을 막는다.
    @Column(unique = true)

    private String name;

    private String address;

    private double latitude;

    private double longitude;

    private String phoneNumber;

    @Builder
    public Cafe(String name, double latitude, double longitude, String address, String phoneNumber) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.phoneNumber = phoneNumber;
    }

    public void update(CafeUpdateRequest request) {
        this.name = request.getCafeName();
        this.address = request.getAddress();
        this.latitude = request.getLatitude();
        this.longitude = request.getLongitude();
        this.phoneNumber = request.getPhoneNumber();
    }
}
