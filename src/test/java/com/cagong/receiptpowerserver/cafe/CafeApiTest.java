package com.cagong.receiptpowerserver.cafe;

import com.cagong.receiptpowerserver.domain.cafe.Cafe;
import com.cagong.receiptpowerserver.domain.cafe.CafeRepository;
import com.cagong.receiptpowerserver.domain.cafe.dto.CafeRequest;
import com.cagong.receiptpowerserver.domain.member.Member;
import com.cagong.receiptpowerserver.domain.member.MemberRepository;
import com.cagong.receiptpowerserver.domain.member.dto.MemberLoginRequest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

// import javax.management.relation.Role; // ✅ 잘못된 import 삭제 확인

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class CafeApiTest {

    // ... (Autowired 필드 동일) ...
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    CafeRepository cafeRepository;
    @Autowired
    PasswordEncoder passwordEncoder;


    @LocalServerPort
    int port;

    private String authorizationValue;
    private String accessToken;


    @BeforeEach
    void setup() {
        RestAssured.port = port;

        // ... (DB 초기화 동일) ...
        memberRepository.deleteAll();
        cafeRepository.deleteAll();


        //멤버 저장
        Member member = Member.builder()
                .username("테스터1")
                .email("mileage123@test.com")
                .password(passwordEncoder.encode("password123"))
                .build();
        memberRepository.save(member);

        //토큰 발급
        MemberLoginRequest request = new MemberLoginRequest(
                "mileage123@test.com",
                "password123"
        );

        accessToken =
                given()
                        .contentType(ContentType.JSON)
                        .body(request)
                        .when()
                        // --- ❗️ [수정 필요] ---
                        // SecurityConfig가 /members/login을 permitAll 하므로 /api 제거
                        .post("/members/login")
                        // --------------------
                        .then()
                        .statusCode(200)
                        .extract()
                        .jsonPath()
                        .getString("accessToken");

        authorizationValue = "Bearer " + accessToken;
        System.out.println("accessToken = " + accessToken);
    }

    // --- (@Test 메서드들은 /api/cafes 경로 사용 유지 - CafeController와 일치 가정) ---
    @Test
    void 전체조회_성공한다() {
        // ... (Cafe 생성 및 저장) ...
        Cafe cafe = Cafe.builder()
                .name("testCafe")
                .address("testAddress")
                .phoneNumber("12341234")
                .build();
        cafeRepository.save(cafe);

        given()
                .header("Authorization", authorizationValue)
                .when()
                .get("/cafes/all") // ✅ CafeController 경로와 일치
                .then()
                // ... (검증) ...
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    void 카페_아이디로_조회_성공한다() {
        // ... (Cafe 생성 및 저장) ...
        Cafe cafe = Cafe.builder()
                .name("testCafe")
                .address("testAddress")
                .phoneNumber("12341234")
                .build();
        Cafe saved = cafeRepository.save(cafe);
        Long cafeId = saved.getId();


        given()
                .header("Authorization", authorizationValue)
                .when()
                .get("/cafes/{cafeId}", cafeId) // ✅ CafeController 경로와 일치
                .then()
                .statusCode(200);
    }

    @Test
    void 카페_생성에_성공한다(){
        CafeRequest request = new CafeRequest(
                "testCafe",
                "testAddress",
                37.111111,
                127.222222,
                "12341234"
        );

        given()
                .header("Authorization", authorizationValue)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/cafes") // ✅ CafeController 경로와 일치
                .then()
                .statusCode(201);
    }

    @Test
    void 삭제_성공() {
        // ... (Cafe 생성 및 저장) ...
        Cafe cafe = Cafe.builder()
                .name("testCafe")
                .address("testAddress")
                .phoneNumber("12341234")
                .build();
        Cafe saved = cafeRepository.save(cafe);
        Long cafeId = saved.getId();

        given()
                .header("Authorization", authorizationValue)
                .when()
                .delete("/cafes/{cafeId}", cafeId) // ✅ CafeController 경로와 일치
                .then()
                .statusCode(204);
    }
}