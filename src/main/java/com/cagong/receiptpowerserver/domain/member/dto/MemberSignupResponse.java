package com.cagong.receiptpowerserver.domain.member.dto;

import com.cagong.receiptpowerserver.domain.member.Member;
import lombok.Getter;

@Getter
public class MemberSignupResponse {
    
    private final Long id;
    private final String username;
    private final String email;
    private final String message;
    
    public MemberSignupResponse(Member member) {
        this.id = member.getId();
        this.username = member.getUsername();
        this.email = member.getEmail();
        this.message = "회원가입이 성공적으로 완료되었습니다.";
    }
}