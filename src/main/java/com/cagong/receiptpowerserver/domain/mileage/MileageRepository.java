package com.cagong.receiptpowerserver.domain.mileage;

import com.cagong.receiptpowerserver.domain.cafe.Cafe;
import com.cagong.receiptpowerserver.domain.member.Member;
import com.cagong.receiptpowerserver.domain.mileage.dto.CafeMileageDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MileageRepository extends JpaRepository<Mileage, Long> {

    List<Mileage> findByMemberId(Long memberId);

    @Query("SELECT m FROM Mileage m JOIN FETCH m.member JOIN FETCH m.cafe WHERE m.member.id = :memberId")
    List<Mileage> findByMemberIdWithFetch(@Param("memberId") Long memberId);

    @Query("select sum(m.point) from Mileage m where m.member.id = :memberId")
    Integer getTotalMileageByMember(@Param("memberId") Long memberId);

    @Query("select new com.cagong.receiptpowerserver.domain.mileage.dto.CafeMileageDto(m.cafe.id, m.cafe.name, sum(m.point)) " +
            "from Mileage m " +
            "where m.member.id = :memberId " +
            "group by m.cafe.id, m.cafe.name")
    List<CafeMileageDto> getMileageByMemberGroupedByCafe(@Param("memberId") Long memberId);

    Optional<Mileage> findByMemberAndCafe(Member member, Cafe cafe);
}