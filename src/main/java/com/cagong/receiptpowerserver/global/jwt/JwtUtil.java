package com.cagong.receiptpowerserver.global.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final long accessTokenExpiration;

    // 여기에 직접 키를 하드코딩합니다.
    private static final String SECRET_KEY_STRING = "ff0d12fcdb370301eef108a0e87970dd3082e23766c077fe1386126fa513d32b";
    private static final long ACCESS_TOKEN_EXPIRATION = 86400000;

    public JwtUtil() {
        // [최종 수정]: Base64 인코딩을 제거하고, 문자열을 직접 바이트로 변환합니다.
        // 이 키는 이미 512비트 HMAC에 적합한 길이의 문자열입니다.
        this.key = Keys.hmacShaKeyFor(SECRET_KEY_STRING.getBytes());
        this.accessTokenExpiration = ACCESS_TOKEN_EXPIRATION;
    }
    
    public String generateAccessToken(Long userId, String username) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("username", username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(key)
                .compact();
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Long.valueOf(claims.getSubject());
    }
    
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.get("username", String.class);
    }
}
