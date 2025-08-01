package com.cagong.receiptpowerserver.domain.member;

import com.cagong.receiptpowerserver.config.JwtUtil;
import com.cagong.receiptpowerserver.domain.member.dto.MemberLoginDto;
import com.cagong.receiptpowerserver.domain.member.dto.MemberRequestDto;
import com.cagong.receiptpowerserver.domain.member.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public Member signup(MemberRequestDto dto) {
        if (memberRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 username입니다.");
        }
        Member member = Member.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .build();
        return memberRepository.save(member);
    }

    @Transactional(readOnly = true)
    public String login(MemberLoginDto dto) {
        Member member = memberRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("가입되지 않은 사용자입니다."));
        if (!passwordEncoder.matches(dto.getPassword(), member.getPassword())) {
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
        }
        return jwtUtil.createToken(member.getUsername());
    }
}
