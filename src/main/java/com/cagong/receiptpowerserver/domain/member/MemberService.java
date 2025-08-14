package com.cagong.receiptpowerserver.domain.member;

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

    /**
     * 회원가입 로직
     */
    @Transactional
    public Member registerMember(MemberRegisterDto dto) {
        // 중복 아이디 체크
        if (memberRepository.findByLoginId(dto.getLoginId()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 로그인 아이디입니다.");
        }

        Member member = Member.builder()
                .nickname(dto.getNickname())
                .loginId(dto.getLoginId())
                .password(passwordEncoder.encode(dto.getPassword())) // 비밀번호 암호화
                .local(dto.getLocal())
                .build();

        return memberRepository.save(member);
    }

    /**
     * 로그인 검증 로직
     */
    public Member login(String loginId, String rawPassword) {
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        if (!passwordEncoder.matches(rawPassword, member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        return member;
    }
}
