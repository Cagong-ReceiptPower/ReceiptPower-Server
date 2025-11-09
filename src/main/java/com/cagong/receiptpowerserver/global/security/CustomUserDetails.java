package com.cagong.receiptpowerserver.global.security;

import com.cagong.receiptpowerserver.domain.member.Member;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections; // List 대신 Collections 사용
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {

    private final Member member; // [수정] Member 객체를 통째로 저장합니다.

    public CustomUserDetails(Member member) {
        this.member = member;
    }

    // --- 최종 수정 ---
    // JWT 토큰 주체와 loadUserByUsername 파라미터를 "이메일"로 통일
    @Override
    public String getUsername() {
        return member.getEmail();
    }

    // --- 동적 권한 ---
    // member 객체에서 실제 Role을 꺼내 동적으로 권한 부여
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return member.getPassword();
    }

    // ... (나머지 4개 메서드는 true로 동일) ...
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}