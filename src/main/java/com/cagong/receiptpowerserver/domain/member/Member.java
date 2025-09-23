package com.cagong.receiptpowerserver.domain.member;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String email;

    @Column(nullable = false, unique = true, length = 20)
    private String username;

    @Column(nullable = false)
    private String password;

    private Double currentLatitude;
    private Double currentLongitude;

    @Builder
    public Member(String username, String email, String password, Double currentLatitude, Double currentLongitude){
        this.username = username;
        this.email = email;
        this.password = password;
        this.currentLatitude = currentLatitude;
        this.currentLongitude = currentLongitude;
    }
}