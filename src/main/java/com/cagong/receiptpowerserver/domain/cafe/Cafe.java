package com.cagong.receiptpowerserver.domain.cafe;

import jakarta.persistence.*;

@Entity
public class Cafe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cafe_id")
    private Long id;

    private String name;

    private String address;

    private String phoneNumber;
}
