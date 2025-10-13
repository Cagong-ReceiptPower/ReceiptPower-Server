package com.cagong.receiptpowerserver.domain.cafe;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CafeRepository extends JpaRepository<Cafe, Long> {
    // [추가] kakaoPlaceId로 Cafe를 찾는 메서드
    Optional<Cafe> findByKakaoPlaceId(String kakaoPlaceId);
}
