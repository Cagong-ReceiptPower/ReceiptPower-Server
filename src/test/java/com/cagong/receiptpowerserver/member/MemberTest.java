package com.cagong.receiptpowerserver.member;

import com.cagong.receiptpowerserver.domain.member.Member;
import com.cagong.receiptpowerserver.domain.member.MemberRepository;
import com.cagong.receiptpowerserver.domain.member.Role; // ❗️ 올바른 Role import 확인!
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder; // ❗️ PasswordEncoder import 확인!
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
public class MemberTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired // ❗️ PasswordEncoder 주입 추가!
    private PasswordEncoder passwordEncoder;

    @Test
    void testSomething() {
        // --- ⬇️ [이 부분을 다시 확인 & 수정] ---
        Member member = Member.builder()
                .email("test@gmail.com")
                .username("testuser")
                // ❗️ passwordEncoder 사용!
                .password(passwordEncoder.encode("password123"))
                .role(Role.USER) // <-- ❗️ 이 줄이 꼭 있어야 합니다!
                .build();
        // ------------------------------------

        // save() 메서드가 반환하는 값을 'saved' 변수에 저장
        Member saved = memberRepository.save(member);

        Optional<Member> found = memberRepository.findById(saved.getId());

        // isPresent() 체크 추가 (더 안전함)
        Assertions.assertThat(found).isPresent();
        Assertions.assertThat(found.get().getUsername()).isEqualTo(member.getUsername());
    }
}