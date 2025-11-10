package com.cagong.receiptpowerserver.domain.mileage;

import com.cagong.receiptpowerserver.domain.cafe.Cafe;
import com.cagong.receiptpowerserver.domain.cafe.CafeRepository;
import com.cagong.receiptpowerserver.domain.member.Member;
import com.cagong.receiptpowerserver.domain.member.MemberRepository;
import com.cagong.receiptpowerserver.domain.mileage.dto.*;
import com.cagong.receiptpowerserver.domain.mqtt.MqttService;
import com.cagong.receiptpowerserver.global.util.MemberUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MileageService {

    private final MileageRepository mileageRepository;
    private final CafeRepository cafeRepository;
    private final MemberRepository memberRepository;
    private final MqttService mqttService;

    public TotalMileageResponse getTotalMileage(){
        Long memberId = MemberUtil.getCurrentMember();

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));

        int totalMileage = mileageRepository.getTotalMileageByMember(memberId);

        List<CafeMileageDto> cafeMileages = mileageRepository.getMileageByMemberGroupedByCafe(memberId);

        return new TotalMileageResponse(member.getUsername(), totalMileage, cafeMileages);
    }

    public CafeMileageResponse getCafeMileage(Long cafeId){
        Long memberId = MemberUtil.getCurrentMember();

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));

        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new RuntimeException("카페를 찾을 수 없습니다."));

        Optional<Mileage> optMileage = mileageRepository.findByMemberAndCafe(member, cafe);

        int mileagePoint = optMileage.map(Mileage::getPoint).orElse(0);
        LocalDateTime mileageUpdatedAt = optMileage.map(Mileage::getUpdatedAt).orElse(null);

        CafeMileageResponse response = new CafeMileageResponse(cafeId, cafe.getName(), mileagePoint, mileageUpdatedAt);
        return response;
    }

    @Transactional
    public SaveMileageResponse addMileage(SaveMileageRequest request){
        Long memberId = MemberUtil.getCurrentMember();
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));

        Cafe cafe = cafeRepository.findById(request.getCafeId())
                .orElseThrow(() -> new RuntimeException("카페를 찾을 수 없습니다."));

        int pointsToAdd = request.getAmount()/25;

        Mileage mileage = mileageRepository.findByMemberAndCafe(member, cafe)
                .map(m -> {
                    m.addPoints(pointsToAdd);
                    return m;
                })
                .orElseGet(() ->{
                    Mileage toSave = Mileage.builder()
                            .point(pointsToAdd)
                            .member(member)
                            .cafe(cafe)
                            .build();
                    return mileageRepository.save(toSave);
                });
        return new SaveMileageResponse(mileage.getId(),mileage.getPoint());
    }

    @Transactional
    public UseMileageResponse startMileageUsage(Long cafeId){
        Long memberId = MemberUtil.getCurrentMember();

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));

        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new RuntimeException("카페를 찾을 수 없습니다."));

        Mileage mileage = mileageRepository.findByMemberAndCafe(member, cafe)
                .orElseThrow(() -> new RuntimeException());

        try {
            mileage.startUsage();
        } catch (IllegalStateException e) {
            throw new IllegalArgumentException(e.getMessage());
        }

        int remainingPoint = mileage.getPoint();

        mqttService.turnOn();
        mqttService.startTimer(cafe.getId(), remainingPoint);
        return new UseMileageResponse(remainingPoint, mileage.getUsageStartTime());
    }

    @Transactional
    public EndMileageUsageResponse endMileageUsage(Long cafeId){
        Long memberId = MemberUtil.getCurrentMember();

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));

        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new RuntimeException("카페를 찾을 수 없습니다."));

        Mileage mileage = mileageRepository.findByMemberAndCafe(member, cafe)
                .orElseThrow(() -> new RuntimeException());

        int remainingMileage = mileage.endUsage();
        EndMileageUsageResponse response = new EndMileageUsageResponse(remainingMileage);

        mqttService.turnOff();
        return response;
    }
}
