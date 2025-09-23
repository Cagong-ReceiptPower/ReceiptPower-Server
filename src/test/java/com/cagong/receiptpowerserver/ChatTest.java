package com.cagong.receiptpowerserver;

import com.cagong.receiptpowerserver.domain.chat.ChatRoomRepository;
import com.cagong.receiptpowerserver.domain.chat.dto.ChatRoomCreateRequest;
import com.cagong.receiptpowerserver.domain.chat.dto.ChatRoomResponse;
import com.cagong.receiptpowerserver.domain.member.Member;
import com.cagong.receiptpowerserver.domain.member.MemberRepository;
import com.cagong.receiptpowerserver.domain.member.dto.MemberLoginRequest;
import com.cagong.receiptpowerserver.domain.member.dto.MemberSignupRequest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ChatTest {

    @LocalServerPort
    int port;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String authorizationValue;
    private Long targetMemberId;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;

        // 테스트 데이터 초기화
        chatRoomRepository.deleteAll();
        memberRepository.deleteAll();

        // 테스트용 회원 생성 (비밀번호 암호화)
        Member member1 = Member.builder()
                .username("chat_user1")
                .email("chat1@test.com")
                .password(passwordEncoder.encode("password123"))
                .currentLatitude(37.5665)
                .currentLongitude(126.9780)
                .build();
        Member member2 = Member.builder()
                .username("chat_user2")
                .email("chat2@test.com")
                .password(passwordEncoder.encode("password123"))
                .currentLatitude(37.5660)
                .currentLongitude(126.9775)
                .build();
        memberRepository.save(member1);
        memberRepository.save(member2);

        targetMemberId = member2.getId();

        // 1. 로그인 요청을 보내 JWT 토큰을 발급받습니다.
        MemberLoginRequest loginRequest = new MemberLoginRequest("chat1@test.com", "password123");
        String accessToken =
                given()
                        .contentType(ContentType.JSON)
                        .body(loginRequest)
                        .when()
                        .post("/members/login")
                        .then()
                        .statusCode(200)
                        .extract()
                        .jsonPath()
                        .getString("accessToken");

        this.authorizationValue = "Bearer " + accessToken;
    }

    @Test
    @DisplayName("1대1 채팅방 생성 성공 테스트")
    void createDirectChatRoom_Success() {
        // Given
        ChatRoomCreateRequest request = new ChatRoomCreateRequest(targetMemberId);

        // When
        ChatRoomResponse response = given()
                .header(AUTHORIZATION, authorizationValue)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/chat-rooms/direct")
                .then()
                .statusCode(200)
                .extract()
                .as(ChatRoomResponse.class);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).contains("chat_user1와 chat_user2의 채팅");
        assertThat(response.getMaxParticipants()).isEqualTo(2);
        assertThat(response.getCurrentParticipants()).isEqualTo(2);
    }
}