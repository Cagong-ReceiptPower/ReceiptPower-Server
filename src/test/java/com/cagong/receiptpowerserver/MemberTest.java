package com.cagong.receiptpowerserver;

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
                .nickname("테스터")
                .loginId("id123")
                .password("password123")
                .local("서울시 마포구")
                .build();
        Member saved = memberRepository.save(member);

        Optional<Member> found = memberRepository.findById(saved.getId());
        Assertions.assertThat(found.get().getNickname()).isEqualTo("테스터");
    }
}
