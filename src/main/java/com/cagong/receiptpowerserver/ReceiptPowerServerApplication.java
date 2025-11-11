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
        // .env 파일 로드
        Dotenv dotenv = Dotenv.configure().load();

        // 환경 변수 설정
        dotenv.entries().forEach(entry ->
                System.setProperty(entry.getKey(), entry.getValue())
        );
        SpringApplication.run(ReceiptPowerServerApplication.class, args);
    }
}