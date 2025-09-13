package com.cagong.receiptpowerserver.domain.cafe;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CafeRepository extends JpaRepository<Cafe, Long> {
}
