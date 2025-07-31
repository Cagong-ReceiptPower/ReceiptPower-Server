package com.cagong.receiptpowerserver.domain.member.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MemberRequestDto {
    private String username;
    private String password;
}