package com.cagong.receiptpowerserver.domain.strip;

import com.cagong.receiptpowerserver.domain.cafe.Cafe;
import jakarta.persistence.*;

@Entity
public class Strip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "strip_id")
    private Long id;

    private int stripNumber;

    private boolean isActive;

    private int time;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cafe_id")
    private Cafe cafe;

}
