package com.cagong.receiptpowerserver.domain.member.dto;

import com.cagong.receiptpowerserver.domain.member.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberLoginResponse {

    private Long id;
    private String email;
    private String username;
    private String accessToken;
    private String tokenType;
    private String message;

    private MemberLoginResponse() {
        this.tokenType = "Bearer";
        this.message = "로그인이 성공적으로 완료되었습니다.";
    }

    public static MemberLoginResponse of(Member member, String accessToken) {
        return new MemberLoginResponse(
                member.getId(),
                member.getEmail(),
                member.getUsername(),
                accessToken,
                "Bearer",
                "로그인이 성공적으로 완료되었습니다."
        );
    }

    // 테스트용
    public static MemberLoginResponse create() {
        return new MemberLoginResponse();
    }

}