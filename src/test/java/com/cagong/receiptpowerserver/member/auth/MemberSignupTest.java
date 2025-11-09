package com.cagong.receiptpowerserver.member.auth; // 패키지 이름은 auth가 아닐 수 있습니다.

import com.cagong.receiptpowerserver.domain.member.Member;
import com.cagong.receiptpowerserver.domain.member.MemberRepository;
import com.cagong.receiptpowerserver.domain.member.MemberService;
import com.cagong.receiptpowerserver.domain.member.dto.MemberSignupRequest;
import com.cagong.receiptpowerserver.domain.member.dto.MemberSignupResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class MemberSignupTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        // 각 테스트 메서드 실행 전에 member 테이블 비우기
        memberRepository.deleteAll();
    }

    @Test
    void 회원가입_성공() {
        // given
        MemberSignupRequest request = new MemberSignupRequest(
                "testuser",
                "test@example.com",
                "password123"
        );

        MemberSignupResponse response = memberService.signup(request);

        // then
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        // assertThat(response.getMessage()).contains("성공적으로 완료"); // MemberSignupResponse에 message 필드가 있다면 주석 해제

        // 실제 저장 확인
        Member savedMember = memberRepository.findById(response.getId()).orElse(null);
        assertThat(savedMember).isNotNull();
        assertThat(savedMember.getPassword()).isNotEqualTo("password123"); // 암호화 확인
    }

    @Test
    void 중복_사용자명으로_회원가입_실패() {
        // given
        // signup 메서드는 @Transactional이므로, 이 데이터는 테스트 종료 후 롤백됨
        memberService.signup(new MemberSignupRequest("duplicate", "test1@example.com", "password123"));

        // when & then
        assertThatThrownBy(() -> {
            memberService.signup(new MemberSignupRequest("duplicate", "test2@example.com", "password456"));
        }).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 사용 중인 사용자명");
    }

    @Test
    void 중복_이메일로_회원가입_실패() {
        // given
        memberService.signup(new MemberSignupRequest("user1", "duplicate@example.com", "password123"));

        // when & then
        assertThatThrownBy(() -> {
            memberService.signup(new MemberSignupRequest("user2", "duplicate@example.com", "password456"));
        }).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 사용 중인 이메일");
    }
}