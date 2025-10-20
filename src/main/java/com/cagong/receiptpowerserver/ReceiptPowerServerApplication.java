package com.cagong.receiptpowerserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing // 0917 JPA Auditing

@SpringBootApplication
public class ReceiptPowerServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReceiptPowerServerApplication.class, args);
    }
}
