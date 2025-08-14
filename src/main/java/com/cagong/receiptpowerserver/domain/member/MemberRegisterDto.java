package com.cagong.receiptpowerserver.domain.member;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class MemberRegisterDto {
    private String nickname;
    private String loginId;
    private String password;
    private String local;

}

