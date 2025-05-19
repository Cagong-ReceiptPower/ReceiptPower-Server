package com.cagong.receiptpowerserver.mileage;

import com.cagong.receiptpowerserver.cafe.Cafe;
import com.cagong.receiptpowerserver.member.Member;
import jakarta.persistence.*;

@Entity
public class Mileage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mileage_id")
    private Long id;

    private int point;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "cafe_id")
    private Cafe cafe;
}
