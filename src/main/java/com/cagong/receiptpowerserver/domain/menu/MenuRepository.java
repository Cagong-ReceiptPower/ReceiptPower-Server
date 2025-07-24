package com.cagong.receiptpowerserver.domain.menu;

import com.cagong.receiptpowerserver.domain.menu.domain.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuRepository extends JpaRepository<Menu, Long> {
}
