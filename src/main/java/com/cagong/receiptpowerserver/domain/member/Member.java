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

    private String nickname;

    private String loginId;

    private String password;

    private String local;

    @Builder
    public  Member(String nickname, String loginId, String password, String local){
        this.nickname = nickname;
        this.loginId = loginId;
        this.password = password;
        this.local = local;
    }
}
