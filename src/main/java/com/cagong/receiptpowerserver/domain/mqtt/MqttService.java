package com.cagong.receiptpowerserver.domain.mqtt;

import com.cagong.receiptpowerserver.domain.cafe.Cafe;
import com.cagong.receiptpowerserver.domain.cafe.CafeRepository;
import com.cagong.receiptpowerserver.domain.member.Member;
import com.cagong.receiptpowerserver.domain.member.MemberRepository;
import com.cagong.receiptpowerserver.domain.mileage.Mileage;
import com.cagong.receiptpowerserver.domain.mileage.MileageRepository;
import com.cagong.receiptpowerserver.global.util.MemberUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MqttService {

    private final MileageRepository mileageRepository;
    private final MemberRepository memberRepository;
    private final CafeRepository cafeRepository;

    public String getTimerMessage(Long cafeId, int time){
        Long memberId = MemberUtil.getCurrentMember();

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));

        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new RuntimeException("카페를 찾을 수 없습니다."));

        Mileage mileage = mileageRepository.findByMemberAndCafe(member, cafe)
                .orElseThrow(() -> new RuntimeException());
        int remainingMileageTime = mileage.getPoint();
        int returnTime = remainingMileageTime + time;
        String message = "time:" + returnTime;

        return message;
    }
}
