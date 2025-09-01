package com.cagong.receiptpowerserver.domain.mileage;

import com.cagong.receiptpowerserver.domain.cafe.Cafe;
import com.cagong.receiptpowerserver.domain.common.BaseEntity;
import com.cagong.receiptpowerserver.domain.member.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Mileage extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mileage_id")
    private Long id;

    private int points;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cafe_id")
    private Cafe cafe;

    private LocalDateTime usageStartTime;

    @Builder
    private Mileage(int points, Member member, Cafe cafe){
        this.points = points;
        this.member = member;
        this.cafe = cafe;
    }

    public void addPoints(int pointsToAdd){
        if (pointsToAdd < 0) {
            throw new IllegalArgumentException("추가할 포인트는 0보다 커야 합니다.");
        }
        this.points += pointsToAdd;
    }

    public void startUsage() {
        if (this.usageStartTime != null) {
            throw new IllegalStateException("이미 사용 중입니다.");
        }
        this.usageStartTime = LocalDateTime.now();
    }

    public int endUsage() {
        if (this.usageStartTime == null) {
            throw new IllegalStateException("사용이 시작되지 않았습니다.");
        }

        long minutes = Duration.between(this.usageStartTime, LocalDateTime.now()).toMinutes();
        int pointsToUse = (int)Math.ceil(minutes/10.0);

        if (this.points < pointsToUse) {
            throw new IllegalStateException("포인트가 부족합니다.");
        }

        this.points -= pointsToUse;
        this.usageStartTime = null;

        return points;
    }
}
