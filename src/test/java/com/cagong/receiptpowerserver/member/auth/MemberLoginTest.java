package com.cagong.receiptpowerserver.member.auth;

import com.cagong.receiptpowerserver.domain.member.MemberService;
import com.cagong.receiptpowerserver.domain.member.dto.MemberSignupRequest;
import com.cagong.receiptpowerserver.domain.member.dto.MemberLoginRequest;
import com.cagong.receiptpowerserver.domain.member.dto.MemberLoginResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class MemberLoginTest {

    @Autowired
    private MemberService memberService;

    @BeforeEach
    void setUp() {
        // 테스트용 회원 생성
        MemberSignupRequest signupRequest = new MemberSignupRequest(
            "testuser", 
            "test@example.com", 
            "password123"
        );
        memberService.signup(signupRequest);
    }

    @Test
    void 사용자명으로_로그인_성공() {
        // given
        MemberLoginRequest request = new MemberLoginRequest("testuser", "password123");

        // when
        MemberLoginResponse response = memberService.login(request);

        // then
        // [수정됨] username 대신 ID와 email 검증
        assertThat(response.getId()).isNotNull();
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getAccessToken()).isNotNull();
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getMessage()).contains("성공적으로 완료");
    }

    @Test
    void 이메일로_로그인_성공() {
        // given
        MemberLoginRequest request = new MemberLoginRequest("test@example.com", "password123");

        // when
        MemberLoginResponse response = memberService.login(request);

        // then
        // [수정됨] username 대신 ID가 null이 아닌지 확인
        assertThat(response.getId()).isNotNull();
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getAccessToken()).isNotNull();
    }

    @Test
    void 잘못된_비밀번호로_로그인_실패() {
        // given
        MemberLoginRequest request = new MemberLoginRequest("testuser", "wrongpassword");

        // when & then
        assertThatThrownBy(() -> {
            memberService.login(request);
        }).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("비밀번호가 일치하지 않습니다");
    }

    @Test
    void 존재하지_않는_사용자로_로그인_실패() {
        // given
        MemberLoginRequest request = new MemberLoginRequest("nonexistent", "password123");

        // when & then
        assertThatThrownBy(() -> {
            memberService.login(request);
        }).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("존재하지 않는 사용자명");
    }
}
