package com.cagong.receiptpowerserver;

import com.cagong.receiptpowerserver.domain.cafe.Cafe;
import com.cagong.receiptpowerserver.domain.cafe.CafeRepository;
import com.cagong.receiptpowerserver.domain.member.Member;
import com.cagong.receiptpowerserver.domain.member.MemberRepository;
import com.cagong.receiptpowerserver.domain.mileage.Mileage;
import com.cagong.receiptpowerserver.domain.mileage.MileageRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.Optional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class MileageTest {
    @Autowired
    private MileageRepository mileageRepository;
    
    @Autowired
    private MemberRepository memberRepository;
    
    @Autowired
    private CafeRepository cafeRepository;

    // 공통 테스트 데이터 생성 메서드
    private Member createTestMember(String username, String email) {
        Member member = Member.builder()
                .username(username)
                .email(email)
                .password("password123")
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
        Member savedMember = createTestMember("마일리지테스터", "mileage@test.com");
        Cafe savedCafe = createTestCafe("스타벅스 강남점", "서울시 강남구 역삼동", "02-1234-5678");

        // When - 마일리지 저장
        Mileage mileage = Mileage.builder()
                .point(1000)
                .member(savedMember)
                .cafe(savedCafe)
                .build();
        Mileage savedMileage = mileageRepository.save(mileage);

        // Then - 검증
        Optional<Mileage> found = mileageRepository.findById(savedMileage.getId());
        Assertions.assertThat(found).isPresent();
        Assertions.assertThat(found.get().getPoint()).isEqualTo(1000);
        
        // 연관관계 검증을 위한 페치 조인 사용 또는 트랜잭션 내에서 접근
        Assertions.assertThat(found.get().getMember().getUsername()).isEqualTo("마일리지테스터");
        Assertions.assertThat(found.get().getCafe().getName()).isEqualTo("스타벅스 강남점");
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
                .password("password123")
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

}
