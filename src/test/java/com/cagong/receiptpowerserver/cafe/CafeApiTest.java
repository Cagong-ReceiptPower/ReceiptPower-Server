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

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class CafeApiTest {

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


    //TODO: setup메서드, 디비 초기화 테스트 패키지 전체화 하기
    @BeforeEach
    void setup() {
        RestAssured.port = port;

        //테스트db 초기화
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
                        .post("/members/login")
                        .then()
                        .statusCode(200)
                        .extract()
                        .jsonPath()
                        .getString("accessToken");

        authorizationValue = "Bearer " + accessToken;
    }

    @Test
    void 전체조회_성공한다() {
        Cafe cafe = Cafe.builder()
                .name("testCafe")
                .address("testAddress")
                .latitude(37.111111)
                .longitude(127.222222)
                .phoneNumber("12341234")
                .build();

        given()
                .header("Authorization", authorizationValue)
                .when()
                .get("/cafes/all")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", greaterThanOrEqualTo(0)); // 카페가 0개 이상
    }

    @Test
    void 카페_아이디로_조회_성공한다() {
        Cafe cafe = Cafe.builder()
                .name("testCafe")
                .address("testAddress")
                .latitude(37.111111)
                .longitude(127.222222)
                .phoneNumber("12341234")
                .build();
        Cafe saved = cafeRepository.save(cafe);

        Long cafeId = saved.getId();

        given()
                .header("Authorization", authorizationValue)
                .when()
                .get("/cafes/{cafeId}", cafeId)
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
                .post("/cafes")
                .then()
                .statusCode(201);
    }

    @Test
    void 삭제_성공() {
        Cafe cafe = Cafe.builder()
                .name("testCafe")
                .address("testAddress")
                .latitude(37.111111)
                .longitude(127.222222)
                .phoneNumber("12341234")
                .build();
        Cafe saved = cafeRepository.save(cafe);

        Long cafeId = saved.getId();

        given()
                .header("Authorization", authorizationValue)
                .when()
                .delete("/cafes/{cafeId}", cafeId)
                .then()
                .statusCode(204);
    }
}
