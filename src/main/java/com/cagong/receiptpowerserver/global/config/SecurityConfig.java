package com.cagong.receiptpowerserver.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration; // CorsConfiguration 추가
import org.springframework.web.cors.CorsConfigurationSource; // CorsConfigurationSource 추가
import org.springframework.web.cors.UrlBasedCorsConfigurationSource; // UrlBasedCorsConfigurationSource 추가

import java.util.Arrays; // Arrays 추가

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults()) // CORS 설정 추가
                .authorizeHttpRequests(auth -> auth
                        // 조회 API는 공개
                        .requestMatchers(HttpMethod.GET, "/api/chat-rooms/**").permitAll()
                        // 생성/상태 변경은 인증 필요
                        .requestMatchers(HttpMethod.POST, "/api/chat-rooms").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/chat-rooms/**").authenticated()
                        .anyRequest().permitAll()
                )
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    // CORS 설정 빈 추가
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:8080", "null")); // 모든 출처 허용. "null"은 로컬 파일 접근을 허용
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")); // 모든 HTTP 메서드 허용
        configuration.setAllowedHeaders(Arrays.asList("*")); // 모든 헤더 허용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}