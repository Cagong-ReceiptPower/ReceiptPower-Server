package com.cagong.receiptpowerserver.domain.oauth;

import com.cagong.receiptpowerserver.domain.member.Member;
import com.cagong.receiptpowerserver.domain.member.MemberRepository;
import com.cagong.receiptpowerserver.domain.member.dto.MemberLoginResponse;
import com.cagong.receiptpowerserver.domain.oauth.dto.OAuthLoginRequest;
import com.cagong.receiptpowerserver.domain.oauth.dto.OAuthUserInfo;
import com.cagong.receiptpowerserver.global.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class OAuthService {

    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    @Transactional
    public MemberLoginResponse login(OAuthLoginRequest request) {
        String provider = request.getProvider();
        String code = request.getAuthorizationCode();

        OAuthUserInfo userInfo;

        if ("google".equalsIgnoreCase(provider)) {
            userInfo = getGoogleUserInfo(code);
        } else {
            throw new IllegalArgumentException("지원하지 않는 provider: " + provider);
        }

        // Member 조회
        Member member = memberRepository.findByEmail(userInfo.getEmail())
                .orElseGet(() -> {                      // 존재하지 않는 멤버일 시 새로 가입
                    Member newMember = Member.builder()
                            .email(userInfo.getEmail())
                            .username(generateUsername(userInfo.getUsername()))
                            .password("")
                            .build();
                    return memberRepository.save(newMember);
                });

        // JWT 발급
        String token = jwtUtil.generateAccessToken(member.getId(), member.getUsername());

        return new MemberLoginResponse(member, token);
    }

    private String generateUsername(String name) {
        // username은 중복 불가이므로 예시로 랜덤 숫자 붙임 예) 유연호123
        return name.replaceAll("\\s+", "") + (int)(Math.random() * 1000);
    }

    private OAuthUserInfo getGoogleUserInfo(String code) {
        // Access Token 요청
        String tokenUrl = "https://oauth2.googleapis.com/token";

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(params, headers);
        ResponseEntity<Map<String, Object>> tokenResponse =
                restTemplate.postForEntity(tokenUrl, tokenRequest, (Class<Map<String, Object>>)(Class)Map.class);

        if (!tokenResponse.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("구글 토큰 요청 실패: " + tokenResponse.getStatusCode());
        }

        Map<String, Object> tokenBody = tokenResponse.getBody();
        if (tokenBody == null || tokenBody.get("access_token") == null) {
            throw new RuntimeException("구글 응답에 access_token 없음");
        }
        String accessToken = (String) tokenBody.get("access_token");

        // 사용자 정보 요청
        HttpHeaders userInfoHeaders = new HttpHeaders();
        userInfoHeaders.setBearerAuth(accessToken);
        HttpEntity<Void> userInfoRequest = new HttpEntity<>(userInfoHeaders);

        String userInfoUrl = "https://www.googleapis.com/oauth2/v2/userinfo";
        ResponseEntity<Map<String, Object>> userInfoResponse =
                restTemplate.exchange(userInfoUrl, HttpMethod.GET, userInfoRequest,
                        (Class<Map<String, Object>>)(Class)Map.class);

        if (!userInfoResponse.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("구글 사용자 정보 요청 실패: " + userInfoResponse.getStatusCode());
        }

        Map<String, Object> userInfoBody = userInfoResponse.getBody();
        if (userInfoBody == null) {
            throw new RuntimeException("구글 사용자 정보 응답이 없음");
        }

        String email = (String) userInfoBody.get("email");
        String name = (String) userInfoBody.get("name");

        return new OAuthUserInfo(email, name);
    }
}