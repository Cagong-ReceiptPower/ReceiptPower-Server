package com.cagong.receiptpowerserver.domain.oauth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OAuthLoginRequest {
    private String provider;
    private String authorizationCode;
}