package com.cagong.receiptpowerserver.domain.mileage;

import com.cagong.receiptpowerserver.domain.member.Member;
import com.cagong.receiptpowerserver.domain.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MileageService {

    private final MileageRepository mileageRepository;
    private final MemberRepository memberRepository;

    /**
     * 회원 ID, 적립할 마일리지 포인트를 받아 마일리지 적립 처리
     */
    @Transactional
    public Mileage saveMileage(Long memberId, int point) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다. ID: " + memberId));

        Mileage mileage = new Mileage();
        mileage.setMember(member);
        mileage.setPoint(point);
        // 필요에 따라 Cafe 정보도 세팅 가능

        return mileageRepository.save(mileage);
    }

    /**
     * 마일리지 사용 (차감)
     */
    @Transactional
    public void useMileage(Long memberId, int amount) {
        Integer totalPoints = getTotalMileage(memberId);
        totalPoints = (totalPoints == null) ? 0 : totalPoints;

        if (totalPoints < amount) {
            throw new IllegalArgumentException("마일리지 잔액이 부족합니다.");
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다. ID: " + memberId));

        Mileage mileageUse = new Mileage();
        mileageUse.setMember(member);
        mileageUse.setPoint(-amount); // 음수로 차감 처리

        mileageRepository.save(mileageUse);
    }

    /**
     * 회원 마일리지 총합 조회
     */
    public Integer getTotalMileage(Long memberId) {
        return mileageRepository.sumMileagePointsByMemberId(memberId);
    }
}
