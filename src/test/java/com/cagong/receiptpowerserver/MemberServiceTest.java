package com.cagong.receiptpowerserver;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Optional;

import com.cagong.receiptpowerserver.domain.member.Member;
import com.cagong.receiptpowerserver.domain.member.MemberRegisterDto;
import com.cagong.receiptpowerserver.domain.member.MemberRepository;
import com.cagong.receiptpowerserver.domain.member.MemberService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberService memberService;

    @Test
    void 회원가입_성공() {
        MemberRegisterDto dto = new MemberRegisterDto();
        dto.setLoginId("testUser");
        dto.setPassword("password");
        dto.setNickname("nickname");
        dto.setLocal("local");

        when(memberRepository.findByLoginId(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Member savedMember = memberService.registerMember(dto);

        assertNotNull(savedMember);
        assertEquals("testUser", savedMember.getLoginId());
        assertEquals("encodedPassword", savedMember.getPassword());
        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    void 회원가입_실패_중복아이디() {
        MemberRegisterDto dto = new MemberRegisterDto();
        dto.setLoginId("duplicateUser");

        Member member = Member.builder()
                .loginId("duplicateUser")
                .password("encodedPassword")
                .nickname("nickname")
                .local("local")
                .build();

        when(memberRepository.findByLoginId(anyString())).thenReturn(Optional.of(member));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            memberService.registerMember(dto);
        });

        assertEquals("이미 존재하는 로그인 아이디입니다.", exception.getMessage());
        verify(memberRepository, never()).save(any());
    }

    @Test
    void 로그인_성공() {
        String rawPassword = "password";
        String encodedPassword = "encodedPassword";

        Member member = Member.builder()
                .loginId("testUser")
                .password(encodedPassword)
                .nickname("nickname")
                .local("local")
                .build();

        when(memberRepository.findByLoginId(anyString())).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);

        Member loggedInMember = memberService.login("testUser", rawPassword);

        assertNotNull(loggedInMember);
        assertEquals("testUser", loggedInMember.getLoginId());
    }

    @Test
    void 로그인_실패_회원없음() {
        when(memberRepository.findByLoginId(anyString())).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            memberService.login("nonexistentUser", "password");
        });

        assertEquals("회원이 존재하지 않습니다.", exception.getMessage());
    }

    @Test
    void 로그인_실패_비밀번호불일치() {
        Member member = Member.builder()
                .loginId("testUser")
                .password("encodedPassword")
                .nickname("nickname")
                .local("local")
                .build();

        when(memberRepository.findByLoginId(anyString())).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            memberService.login("testUser", "wrongPassword");
        });

        assertEquals("비밀번호가 일치하지 않습니다.", exception.getMessage());
    }
}
