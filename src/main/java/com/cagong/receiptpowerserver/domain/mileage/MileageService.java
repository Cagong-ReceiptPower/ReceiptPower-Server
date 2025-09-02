package com.cagong.receiptpowerserver.domain.mileage;

import com.cagong.receiptpowerserver.domain.cafe.Cafe;
import com.cagong.receiptpowerserver.domain.cafe.CafeRepository;
import com.cagong.receiptpowerserver.domain.member.Member;
import com.cagong.receiptpowerserver.domain.member.MemberRepository;
import com.cagong.receiptpowerserver.domain.mileage.dto.*;
import com.cagong.receiptpowerserver.global.util.MemberUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MileageService {

    private final MileageRepository mileageRepository;
    private final CafeRepository cafeRepository;
    private final MemberRepository memberRepository;

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
                .orElseThrow(() -> new IllegalArgumentException());

        Mileage mileage = mileageRepository.findByMemberAndCafe(member, cafe)
                .orElseThrow(() -> new IllegalArgumentException());

        CafeMileageResponse response = new CafeMileageResponse(cafeId, cafe.getName(), mileage.getPoint(), mileage.getUpdatedAt());
        return response;
    }

    @Transactional
    public void addMileage(SaveMileageRequest request){
        Long memberId = MemberUtil.getCurrentMember();
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));

        Cafe cafe = cafeRepository.findById(request.getCafeId())
                .orElseThrow(() -> new IllegalArgumentException());

        int pointsToAdd = (int) Math.ceil(request.getRemainingTime()/10.0);

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
    }

    @Transactional
    public void startMileageUsage(Long cafeId){
        Long memberId = MemberUtil.getCurrentMember();
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));

        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new IllegalArgumentException());

        Mileage mileage = mileageRepository.findByMemberAndCafe(member, cafe)
                .orElseThrow(() -> new RuntimeException());

        mileage.startUsage();
    }

    @Transactional
    public EndMileageUsageResponse endMileageUsage(Long cafeId){
        Long memberId = MemberUtil.getCurrentMember();
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));

        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new IllegalArgumentException());

        Mileage mileage = mileageRepository.findByMemberAndCafe(member, cafe)
                .orElseThrow(() -> new RuntimeException());

        int remainingMileage = mileage.endUsage();
        EndMileageUsageResponse response = new EndMileageUsageResponse(remainingMileage);
        return response;
    }
}
