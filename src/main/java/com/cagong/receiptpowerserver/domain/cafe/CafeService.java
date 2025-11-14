package com.cagong.receiptpowerserver.domain.cafe;

import com.cagong.receiptpowerserver.domain.cafe.dto.CafeRequest;
import com.cagong.receiptpowerserver.domain.cafe.dto.CafeUpdateRequest; // [수정] 1. CafeUpdateRequest import
import com.cagong.receiptpowerserver.domain.cafe.dto.CafeResponse;
// [수정] 2. Kakao API 및 ChatRoomService 관련 의존성 *모두 삭제*
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class CafeService {

    private final CafeRepository cafeRepository;

    // [수정] 3. ChatRoomService, RestTemplate, kakaoApiKey 의존성 *모두 삭제*

    /**
     * 1. 카페 생성 (POST /api/cafes)
     */
    @Transactional
    public Long saveCafe(CafeRequest request) {
        Cafe cafe = Cafe.builder()
                .name(request.getCafeName())
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .phoneNumber(request.getPhoneNumber())
                .build();

        Cafe savedCafe = cafeRepository.save(cafe);
        return savedCafe.getId();
    }

    /**
     * 2. 카페 전체 조회 (GET /api/cafes/all)
     */
    @Transactional(readOnly = true)
    public List<CafeResponse> findAllCafes() {
        return cafeRepository.findAll().stream()
                .map(CafeResponse::new) // (CafeResponse(Cafe) 생성자 사용)
                .collect(Collectors.toList());
    }

    /**
     * 3. 카페 ID로 1건 조회 (GET /api/cafes/{cafeId})
     */
    @Transactional(readOnly = true)
    public CafeResponse findCafeById(Long cafeId) {
        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new RuntimeException("해당 ID의 카페를 조회할 수 없습니다:" + cafeId));
        return new CafeResponse(cafe); // (CafeResponse(Cafe) 생성자 사용)
    }

    /**
     * 4. 카페 삭제 (DELETE /api/cafes/{cafeId})
     */
    @Transactional
    public void deleteCafeById(Long cafeId) {
        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new RuntimeException("해당 ID의 카페를 찾을 수 없습니다: " + cafeId));
        cafeRepository.delete(cafe);
    }

    /**
     * 5. 카페 수정
     * */
    @Transactional
    public CafeResponse updateCafe(Long cafeId, CafeUpdateRequest request) {
        // [수정] 4. updateCafe 로직을 올바르게 수정

        // 1. DB에서 카페 Entity를 조회
        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new RuntimeException("해당 ID의 카페를 찾을 수 없습니다: " + cafeId));

        // 2. Entity의 update 메서드 호출 (JPA 변경 감지)
        //    (이 코드가 작동하려면 Cafe.java에 update(CafeUpdateRequest request) 메서드가 있어야 합니다)
        cafe.update(request);

        // 3. 수정된 Entity를 DTO로 변환하여 반환
        return new CafeResponse(cafe);
    }
}