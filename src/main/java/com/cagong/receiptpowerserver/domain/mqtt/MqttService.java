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
    private final MqttPublisher publisher;

    public void turnOn(){
        publisher.publishCommand("mycafe/relay/control", "on");
    }

    public void turnOff(){
        publisher.publishCommand("mycafe/relay/control", "off");
    }

    public String startTimer(Long cafeId, int time){
        Long memberId = MemberUtil.getCurrentMember();

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));

        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new RuntimeException("카페를 찾을 수 없습니다."));

        String message = "time:" + time;

        publisher.publishCommand("mycafe/relay/control", message);

        return message;
    }
}
