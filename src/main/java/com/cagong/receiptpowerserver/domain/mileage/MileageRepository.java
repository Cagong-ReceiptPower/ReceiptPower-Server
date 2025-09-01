package com.cagong.receiptpowerserver.domain.mileage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface MileageRepository extends JpaRepository<Mileage, Long> {
    List<Mileage> findByMemberId(Long memberId);
    
    @Query("SELECT m FROM Mileage m JOIN FETCH m.member JOIN FETCH m.cafe WHERE m.member.id = :memberId")
    List<Mileage> findByMemberIdWithFetch(@Param("memberId") Long memberId);
}
