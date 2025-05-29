package com.cagong.receiptpowerserver;

import com.cagong.receiptpowerserver.member.Member;
import com.cagong.receiptpowerserver.member.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MemberTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void 멤버_생성_성공() {
        Member member = Member.builder()
                .nickname("테스터")
                .loginId("id123")
                .password("password123")
                .local("서울시 마포구")
                .build();
        memberRepository.save(member);
    }
}
