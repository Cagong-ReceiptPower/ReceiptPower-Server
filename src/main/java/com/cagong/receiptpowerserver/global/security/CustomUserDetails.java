// global/security/CustomUserDetails.java

package com.cagong.receiptpowerserver.global.security;

import com.cagong.receiptpowerserver.domain.member.Member;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final String email;
    private final String password;
    private final String username; // [추가] 사용자 이름을 저장할 필드

    public CustomUserDetails(Member member) {
        this.id = member.getId();
        this.email = member.getEmail();
        this.password = member.getPassword();
        this.username = member.getUsername(); // [추가] member 객체로부터 username을 가져와 저장
    }

    @Override
    public String getUsername() {
        // [수정] ID 대신 실제 사용자 이름을 반환하도록 변경
        return username;
    }

    // ... 나머지 코드는 그대로 ...
    @Override
    public String getPassword() { return password; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { return List.of(new SimpleGrantedAuthority("ROLE_USER")); }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}