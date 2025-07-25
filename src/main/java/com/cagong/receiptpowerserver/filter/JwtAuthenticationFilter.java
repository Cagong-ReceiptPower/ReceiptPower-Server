package com.cagong.receiptpowerserver.filter;

import com.cagong.receiptpowerserver.config.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Authorization 헤더에서 JWT 추출
        final String authHeader = request.getHeader("Authorization");
        String username = null;
        String jwt = null;

        // 2. Bearer 타입(토큰)이 있으면 파싱
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7); // "Bearer " 이후부터가 순수 토큰
            try {
                username = jwtUtil.getUsernameFromToken(jwt);
            } catch (Exception e) {
                // 유효하지 않은 토큰: 그냥 무시(로그인 필수 API에서는 어차피 인증 실패로 처리됨)
            }
        }

        // 3. JWT 유효성 검증 및 SecurityContext에 인증 정보 저장
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (jwtUtil.validateToken(jwt)) {
                UserDetails userDetails = User.withUsername(username).password("").authorities(Collections.emptyList()).build();
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // 4. 나머지 필터 체인 계속 진행
        filterChain.doFilter(request, response);
    }
}
