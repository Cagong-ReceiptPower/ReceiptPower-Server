package com.cagong.receiptpowerserver.domain.member;

import com.cagong.receiptpowerserver.domain.member.dto.MemberSignupRequest;
import com.cagong.receiptpowerserver.domain.member.dto.MemberSignupResponse;
import com.cagong.receiptpowerserver.domain.member.dto.MemberLoginRequest;
import com.cagong.receiptpowerserver.domain.member.dto.MemberLoginResponse;
import com.cagong.receiptpowerserver.global.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // 회원가입
    @Transactional
    public MemberSignupResponse signup(MemberSignupRequest request) {
        // 1. 중복 확인
        validateDuplicateMember(request.getUsername(), request.getEmail());
        
        // 2. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        
        // 3. Member 엔티티 생성 및 저장
        Member member = Member.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(encodedPassword)
                .role(Role.USER)
                .build();
        
        Member savedMember = memberRepository.save(member);
        
        return new MemberSignupResponse(savedMember);
    }

    //로그인
    @Transactional(readOnly = true)
    public MemberLoginResponse login(MemberLoginRequest request) {

        // 1. 사용자 찾기 (username 또는 email로)
        Member member = findMemberByUsernameOrEmail(request.getUsernameOrEmail());
        
        // 2. 비밀번호 확인
        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        
        // 3. JWT 토큰 생성
        String accessToken = jwtUtil.generateAccessToken(member.getId(), member.getId().toString());
        
        return new MemberLoginResponse(member, accessToken);
    }

    // 검증
    private Member findMemberByUsernameOrEmail(String usernameOrEmail) {
        // 이메일 형식인지 확인 (간단한 체크)
        if (usernameOrEmail.contains("@")) {
            return memberRepository.findByEmail(usernameOrEmail)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다: " + usernameOrEmail));
        } else {
            return memberRepository.findByUsername(usernameOrEmail)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자명입니다: " + usernameOrEmail));
        }
    }

    // 중복 방지
    private void validateDuplicateMember(String username, String email) {
        if (memberRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("이미 사용 중인 사용자명입니다: " + username);
        }
        
        if (memberRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다: " + email);
        }
    }
}
