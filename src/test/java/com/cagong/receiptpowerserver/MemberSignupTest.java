package com.cagong.receiptpowerserver;

import com.cagong.receiptpowerserver.domain.member.Member;
import com.cagong.receiptpowerserver.domain.member.MemberRepository;
import com.cagong.receiptpowerserver.domain.member.MemberService;
import com.cagong.receiptpowerserver.domain.member.dto.MemberSignupRequest;
import com.cagong.receiptpowerserver.domain.member.dto.MemberSignupResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class MemberSignupTest {

    @Autowired
    private MemberService memberService;
    
    @Autowired
    private MemberRepository memberRepository;

    @Test
    void 회원가입_성공() {
        // given
        MemberSignupRequest request = new MemberSignupRequest(
            "testuser", 
            "test@example.com", 
            "password123"
        );

        // when
        MemberSignupResponse response = memberService.signup(request);

        // then
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getMessage()).contains("성공적으로 완료");
        
        // 실제 저장 확인
        Member savedMember = memberRepository.findById(response.getId()).orElse(null);
        assertThat(savedMember).isNotNull();
        assertThat(savedMember.getPassword()).isNotEqualTo("password123"); // 암호화 확인
    }

    @Test
    void 중복_사용자명으로_회원가입_실패() {
        // given
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
