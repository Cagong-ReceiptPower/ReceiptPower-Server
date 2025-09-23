package com.cagong.receiptpowerserver.domain.member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUsername(String username);
    Optional<Member> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @Query("SELECT m FROM Member m WHERE m.id != :memberId " +
            "AND m.currentLatitude BETWEEN :minLat AND :maxLat " +
            "AND m.currentLongitude BETWEEN :minLon AND :maxLon")
    List<Member> findNearbyMembers(@Param("memberId") Long memberId,
                                   @Param("minLat") Double minLat,
                                   @Param("maxLat") Double maxLat,
                                   @Param("minLon") Double minLon,
                                   @Param("maxLon") Double maxLon);
}