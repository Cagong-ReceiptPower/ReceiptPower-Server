package com.cagong.receiptpowerserver.review;

import com.cagong.receiptpowerserver.cafe.Cafe;
import com.cagong.receiptpowerserver.common.BaseEntity;
import com.cagong.receiptpowerserver.member.Member;
import jakarta.persistence.*;

@Entity
public class Review extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    private int score;

    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cafe_id")
    private Cafe cafe;
}
