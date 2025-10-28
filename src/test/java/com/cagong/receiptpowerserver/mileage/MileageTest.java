package com.cagong.receiptpowerserver.mileage;

import com.cagong.receiptpowerserver.domain.cafe.Cafe;
import com.cagong.receiptpowerserver.domain.cafe.CafeRepository;
import com.cagong.receiptpowerserver.domain.member.Member;
import com.cagong.receiptpowerserver.domain.member.MemberRepository;
import com.cagong.receiptpowerserver.domain.member.MemberService;
import com.cagong.receiptpowerserver.domain.member.dto.MemberLoginRequest;
import com.cagong.receiptpowerserver.domain.member.dto.MemberLoginResponse;
import com.cagong.receiptpowerserver.domain.mileage.Mileage;
import com.cagong.receiptpowerserver.domain.mileage.MileageRepository;
import com.cagong.receiptpowerserver.domain.mileage.MileageService;
import com.cagong.receiptpowerserver.domain.mileage.dto.CafeMileageResponse;
import com.cagong.receiptpowerserver.domain.mileage.dto.EndMileageUsageResponse;
import com.cagong.receiptpowerserver.domain.mileage.dto.SaveMileageRequest;
import com.cagong.receiptpowerserver.domain.mileage.dto.SaveMileageResponse;
import com.cagong.receiptpowerserver.global.jwt.JwtUtil;
import com.cagong.receiptpowerserver.global.security.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.apache.http.HttpStatus;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import com.cagong.receiptpowerserver.domain.member.Role;
import java.util.List;

import static io.restassured.RestAssured.*;
import static jakarta.servlet.http.HttpServletResponse.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
//@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class MileageTest {

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private MileageRepository mileageRepository;
    @Autowired
    private MileageService mileageService;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private MemberService memberService;

    @Autowired
    private CafeRepository cafeRepository;
    @Autowired
    protected ObjectMapper mapper;

    @PersistenceContext
    private EntityManager entityManager;

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
        mileageRepository.deleteAll();

        //멤버 저장
        Member member = Member.builder()
                .username("테스터1")
                .email("mileage123@test.com")
                .password(passwordEncoder.encode("password123"))
                .role(Role.USER)
                .build();
        memberRepository.save(member);
/*

        Cafe cafe = Cafe.builder()
                .name("testCafe")
                .address("testAddress")
                .phoneNumber("12341234")
                .build();
        cafeRepository.save(cafe);

        Mileage mileage = Mileage.builder()
                .point(5)
                .member(member)
                .cafe(cafe)
                .build();
        mileageRepository.save(mileage);

*/
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

        System.out.println("배리어: " + authorizationValue);

        // SecurityContext에 세팅
        UserDetails userDetails = new CustomUserDetails(member);
        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // 공통 테스트 데이터 생성 메서드
    private Member createTestMember(String username, String email) {
        Member member = Member.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode("password123"))
                .role(Role.USER)
                .build();
        return memberRepository.save(member);
    }
    
    private Cafe createTestCafe(String name, String address, String phoneNumber) {
        Cafe cafe = Cafe.builder()
                .name(name)
                .address(address)
                .phoneNumber(phoneNumber)
                .build();
        return cafeRepository.save(cafe);
    }

    @Test
    @DisplayName("마일리지 저장 및 조회 테스트")
    void 마일리지_저장_및_조회_테스트() {
        // Given - 테스트 데이터 준비
        // Member savedMember = createTestMember("마일리지테스터", "mileage@test.com"); // ❗️ 이 줄 삭제! (setup에서 로그인한 유저 사용)
        Cafe savedCafe = createTestCafe("스타벅스 강남점", "서울시 강남구 역삼동", "02-1234-5678");

        // When - 마일리지 저장 (setup에서 로그인한 유저 기준으로 적립됨)
        SaveMileageRequest request = new SaveMileageRequest(savedCafe.getId(), 100);
        SaveMileageResponse saveResponse = mileageService.addMileage(request); // 반환 타입이 있다면 받기 (없으면 void)

        // Then - 검증 (setup에서 로그인한 유저 기준으로 조회됨)
        CafeMileageResponse response = mileageService.getCafeMileage(savedCafe.getId());

        Assertions.assertThat(response.getPoints()).isEqualTo(100);
        Assertions.assertThat(response.getCafeId()).isEqualTo(savedCafe.getId()); // Cafe ID 검증 추가

        }

    @Test
    @DisplayName("한 회원의 여러 마일리지 적립 테스트")
    void 한_회원의_여러_마일리지_적립_테스트() {
        // Given
        Member savedMember = createTestMember("포인트수집가", "collector@test.com");
        Cafe savedCafe1 = createTestCafe("이디야 신촌점", "서울시 서대문구 신촌동", "02-111-2222");
        Cafe savedCafe2 = createTestCafe("커피빈 홍대점", "서울시 마포구 홍익동", "02-333-4444");

        // When - 여러 마일리지 적립
        Mileage mileage1 = Mileage.builder()
                .point(500)
                .member(savedMember)
                .cafe(savedCafe1)
                .build();
        
        Mileage mileage2 = Mileage.builder()
                .point(1500)
                .member(savedMember)
                .cafe(savedCafe2)
                .build();

        mileageRepository.save(mileage1);
        mileageRepository.save(mileage2);

        // Then - 효율적인 조회 사용
        List<Mileage> memberMileages = mileageRepository.findByMemberId(savedMember.getId());

        Assertions.assertThat(memberMileages).hasSize(2);
        
        int totalPoints = memberMileages.stream()
                .mapToInt(Mileage::getPoint)
                .sum();
        
        Assertions.assertThat(totalPoints).isEqualTo(2000);
    }

    @Test
    @DisplayName("마일리지 엔티티 연관관계 테스트")
    void 마일리지_엔티티_연관관계_테스트() {
        // Given
        Member member = Member.builder()
                .username("연관관계테스터")
                .email("relation@test.com")
                .password(passwordEncoder.encode("password123"))
                .role(Role.USER)
                .build();
        Member savedMember = memberRepository.save(member);

        Cafe cafe = Cafe.builder()
                .name("투썸플레이스 종로점")
                .address("서울시 종로구 종로1가")
                .phoneNumber("02-555-6666")
                .build();
        Cafe savedCafe = cafeRepository.save(cafe);

        // When
        Mileage mileage = Mileage.builder()
                .point(2500)
                .member(savedMember)
                .cafe(savedCafe)
                .build();
        Mileage savedMileage = mileageRepository.save(mileage);

        // Then - 연관관계 검증
        Assertions.assertThat(savedMileage.getMember()).isNotNull();
        Assertions.assertThat(savedMileage.getCafe()).isNotNull();
        Assertions.assertThat(savedMileage.getMember().getEmail()).isEqualTo("relation@test.com");
        Assertions.assertThat(savedMileage.getCafe().getAddress()).isEqualTo("서울시 종로구 종로1가");
    }

    @Test
    @DisplayName("마일리지 포인트 검증 테스트 - 음수 포인트 포함")
    void 마일리지_포인트_검증_테스트() {
        // Given
        Member savedMember = createTestMember("포인트테스터", "points@test.com");
        Cafe savedCafe = createTestCafe("카페베네 성수점", "서울시 성동구 성수동", "02-777-8888");

        // When & Then - 다양한 포인트 값 테스트
        Mileage mileage1 = Mileage.builder()
                .point(0)  // 0점도 허용
                .member(savedMember)
                .cafe(savedCafe)
                .build();
        
        Mileage mileage2 = Mileage.builder()
                .point(10000)  // 큰 포인트도 허용
                .member(savedMember)
                .cafe(savedCafe)
                .build();

        Mileage saved1 = mileageRepository.save(mileage1);
        Mileage saved2 = mileageRepository.save(mileage2);

        Assertions.assertThat(saved1.getPoint()).isEqualTo(0);
        Assertions.assertThat(saved2.getPoint()).isEqualTo(10000);
        
        // 음수 포인트 테스트 추가 (비즈니스 로직에 따라)
        Mileage negativePointMileage = Mileage.builder()
                .point(-500)  // 음수 포인트 (차감 등의 용도)
                .member(savedMember)
                .cafe(savedCafe)
                .build();
        
        Mileage savedNegative = mileageRepository.save(negativePointMileage);
        Assertions.assertThat(savedNegative.getPoint()).isEqualTo(-500);
    }


    @Test
    void 마일리지_적립_및_총_마일리지_조회_성공(){
        Cafe cafe = Cafe.builder()
                .name("testCafe")
                .address("testAddress")
                .phoneNumber("12341234")
                .build();
        cafeRepository.save(cafe);

        SaveMileageRequest request = new SaveMileageRequest(cafe.getId(), 100);

        given().
                header(AUTHORIZATION, authorizationValue).
                contentType(ContentType.JSON).
                body(request).
        when().
                post("/mileages").
        then().
                statusCode(SC_OK);

        given().
                header(AUTHORIZATION, authorizationValue).
        when().
                get("/mileages/total").
        then().
                statusCode(HttpStatus.SC_OK);
    }

    @Test
    void 카페별_마일리지_조회_성공(){
        Cafe cafe = Cafe.builder()
                .name("testCafe")
                .address("testAddress")
                .phoneNumber("12341234")
                .build();
        cafeRepository.save(cafe);

        SaveMileageRequest request = new SaveMileageRequest(cafe.getId(), 100);

        given().
                header(AUTHORIZATION, authorizationValue).
                contentType(ContentType.JSON).
                body(request).
        when().
                post("/mileages").
        then().
                statusCode(SC_OK);

        CafeMileageResponse response =
        given()
                .header(AUTHORIZATION, authorizationValue)
                .queryParam("cafeId", cafe.getId()).
        when()
                .get("/mileages").
        then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .as(CafeMileageResponse.class);

        assertThat(response.getCafeId()).isEqualTo(cafe.getId());
        assertThat(response.getPoints()).isEqualTo(100);
    }

    @Test
    void 마일리지_사용_및_종료_성공(){
        Cafe cafe = Cafe.builder()
                .name("testCafe")
                .address("testAddress")
                .phoneNumber("12341234")
                .build();
        cafeRepository.save(cafe);

        SaveMileageRequest request = new SaveMileageRequest(cafe.getId(), 1000);

        given().
                header(AUTHORIZATION, authorizationValue).
                contentType(ContentType.JSON).
                body(request).
                when().
                post("/mileages").
                then().
                statusCode(SC_OK);

        //마일리지 사용
        given()
                .header(AUTHORIZATION, authorizationValue)
                .accept(ContentType.JSON)
                .when()
                .post("/mileages/{cafeId}/usage", cafe.getId())
                .then()
                .statusCode(SC_OK);

        //마일리지 종료
        EndMileageUsageResponse endResponse =
                given()
                        .header(AUTHORIZATION, authorizationValue)
                        .accept(ContentType.JSON)
                        .when()
                        .post("/mileages/{cafeId}/usage/end", cafe.getId())
                        .then()
                        .statusCode(SC_OK)
                        .extract()
                        .as(EndMileageUsageResponse.class);

        assertThat(endResponse.getRemainingMileage()).isGreaterThan(0);
    }

    @Test
    void 마일리지가_없으면_사용_실패(){
        Cafe cafe = Cafe.builder()
                .name("testCafe")
                .address("testAddress")
                .phoneNumber("12341234")
                .build();
        cafeRepository.save(cafe);

        SaveMileageRequest request = new SaveMileageRequest(cafe.getId(), 0);

        given().
                header(AUTHORIZATION, authorizationValue).
                contentType(ContentType.JSON).
                body(request).
                when().
                post("/mileages").
                then().
                statusCode(SC_OK);

        //마일리지 사용
        //String errorMessage =
        given()
                .header(AUTHORIZATION, authorizationValue)
                .accept(ContentType.JSON)
                .when()
                .post("/mileages/{cafeId}/usage", cafe.getId())
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
//                .extract()
//                .jsonPath()
//                .get("message");

        //assertThat(errorMessage).isEqualTo("마일리지가 부족하여 사용할 수 없습니다.");
    }
}
