// ReceiptPowerServerApplication.java

package com.cagong.receiptpowerserver;

import io.github.cdimascio.dotenv.Dotenv; // import 추가
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class ReceiptPowerServerApplication {
    public static void main(String[] args) {
        // .env 파일을 로드하고, 그 안의 변수들을 시스템 환경변수로 설정합니다.
        Dotenv dotenv = Dotenv.load();
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        SpringApplication.run(ReceiptPowerServerApplication.class, args);
    }
}