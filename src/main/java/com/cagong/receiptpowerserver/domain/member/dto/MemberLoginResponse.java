package com.cagong.receiptpowerserver.domain.member.dto;

import com.cagong.receiptpowerserver.domain.member.Member;
import lombok.Getter;

@Getter
public class MemberLoginResponse {

    private Long id;
    private String email;
    private String username;
    private String accessToken;
    private String tokenType;
    private String message;

    public MemberLoginResponse() {
        this.tokenType = "Bearer";
        this.message = "로그인이 성공적으로 완료되었습니다.";
    }

    public MemberLoginResponse(Member member, String accessToken) {
        this.id = member.getId();
        this.email = member.getEmail();
        this.username = member.getUsername();
        this.accessToken = accessToken;
        this.tokenType = "Bearer";
        this.message = "로그인이 성공적으로 완료되었습니다.";
    }
}