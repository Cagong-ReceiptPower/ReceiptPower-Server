package com.cagong.receiptpowerserver.domain.member.dto;

import com.cagong.receiptpowerserver.domain.member.Member;
import lombok.Getter;

@Getter
public class MemberLoginResponse {

    private final Long id;
    private final String email;
    private final String accessToken;
    private final String tokenType;
    private final String message;

    public MemberLoginResponse(Member member, String accessToken) {
        this.id = member.getId();
        this.email = member.getEmail();
        this.accessToken = accessToken;
        this.tokenType = "Bearer";
        this.message = "로그인이 성공적으로 완료되었습니다.";
    }
}