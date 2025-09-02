package com.cagong.receiptpowerserver.domain.member.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor
public class MemberLoginRequest {
    
    @NotBlank(message = "사용자명 또는 이메일은 필수입니다")
    private String usernameOrEmail;
    
    @NotBlank(message = "비밀번호는 필수입니다")
    private String password;
    
    public MemberLoginRequest(String usernameOrEmail, String password) {
        this.usernameOrEmail = usernameOrEmail;
        this.password = password;
    }
}
