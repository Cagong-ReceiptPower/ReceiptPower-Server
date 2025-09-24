package com.cagong.receiptpowerserver.domain.cafe;

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

    private String name;

    private String address;

    private double latitude;

    private double longitude;

    private String phoneNumber;

    @Builder
    public Cafe(String name, String address, double latitude, double longitude, String phoneNumber) {
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this. longitude = longitude;
        this.phoneNumber = phoneNumber;
    }
}
