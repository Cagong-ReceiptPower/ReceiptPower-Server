package com.cagong.receiptpowerserver.member;

import com.cagong.receiptpowerserver.domain.member.Member;
import com.cagong.receiptpowerserver.domain.member.MemberRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
public class MemberTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void testSomething() {
        Member member = Member.builder()
                .username("테스터")
                .email("test@gmail.com")
                .password("password123")
                .build();
        Member saved = memberRepository.save(member);

        Optional<Member> found = memberRepository.findById(saved.getId());
        Assertions.assertThat(found.get().getUsername()).isEqualTo("테스터");
    }
}
