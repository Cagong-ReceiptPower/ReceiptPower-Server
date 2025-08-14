package com.cagong.receiptpowerserver.domain.mileage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MileageRepository extends JpaRepository<Mileage, Long> {

    /**
     * 특정 회원의 총 마일리지 합산을 조회하는 메서드
     * @param memberId 회원 ID
     * @return 마일리지 총합 (적립 - 사용)
     */
    @Query("SELECT COALESCE(SUM(m.point), 0) FROM Mileage m WHERE m.member.id = :memberId")
    Integer sumMileagePointsByMemberId(@Param("memberId") Long memberId);
}
