package com.cagong.receiptpowerserver.menu;

import com.cagong.receiptpowerserver.menu.domain.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuRepository extends JpaRepository<Menu, Long> {
}
