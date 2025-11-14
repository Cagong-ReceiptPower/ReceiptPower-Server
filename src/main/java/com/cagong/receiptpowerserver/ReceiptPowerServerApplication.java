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
        Dotenv.configure()
                .ignoreIfMissing() // .env 파일이 없어도(예: 배포 환경) 오류 없이 통과
                .systemProperties() // .env의 모든 변수를 System.setProperty()로 자동 등록
                .load();
        SpringApplication.run(ReceiptPowerServerApplication.class, args);
    }
}