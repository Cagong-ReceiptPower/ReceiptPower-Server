
// domain/cafe/CafeService.java

package com.cagong.receiptpowerserver.domain.cafe;

import com.cagong.receiptpowerserver.domain.cafe.dto.CafeRequest;
import com.cagong.receiptpowerserver.domain.cafe.dto.CafeUpdateRequest;
import com.cagong.receiptpowerserver.domain.cafe.dto.CafeResponse;
import com.cagong.receiptpowerserver.domain.chat.ChatRoomService;
import com.cagong.receiptpowerserver.domain.chat.dto.ChatRoomResponse;
import com.cagong.receiptpowerserver.domain.cafe.dto.CafeWithChatRoomsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CafeService {

    private final CafeRepository cafeRepository;
    private final ChatRoomService chatRoomService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    /**
     * 1. 카페 생성 (POST /api/cafes)
     */
    @Transactional
    public Long saveCafe(CafeRequest request) {
        Cafe cafe = Cafe.builder()
                .name(request.getCafeName()) // ❗️ [수정] .getCafeName()
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
                .map(CafeResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * 3. 카페 ID로 1건 조회 (GET /api/cafes/{cafeId})
     */
    @Transactional(readOnly = true)
    public CafeResponse findCafeById(Long cafeId) {
        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new RuntimeException("해당 ID의 카페를 조회할 수 없습니다:" + cafeId));
        return new CafeResponse(cafe);
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
    @Transactional // (readOnly = false. DB를 수정합니다.)
    public CafeResponse updateCafe(Long cafeId, CafeUpdateRequest request) {

        // 1. DB에서 카페 Entity를 조회 (JPA가 영속성 컨텍스트에서 관리 시작)
        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new RuntimeException("해당 ID의 카페를 찾을 수 없습니다: " + cafeId));
        return new CafeResponse(
                cafe.getId(),
                cafe.getName(),
                cafe.getAddress(),
                cafe.getLatitude(),
                cafe.getLongitude(),
                cafe.getPhoneNumber()
        );
    }

    @Transactional
    public CafeWithChatRoomsResponse findOrCreateCafeByQueryAndGetDetails(String query) {
        // 1. 카카오 API를 호출하여 검색어에 해당하는 장소 정보를 가져옵니다.
        Map<String, Object> kakaoCafeInfo = callKakaoPlaceSearchApi(query);
        String kakaoPlaceId = (String) kakaoCafeInfo.get("id");

        // 2. 카카오 ID를 기준으로 우리 DB에 카페가 이미 있는지 찾아봅니다.
        Optional<Cafe> optionalCafe = cafeRepository.findByKakaoPlaceId(kakaoPlaceId);

        Cafe cafe;
        if (optionalCafe.isPresent()) {
            cafe = optionalCafe.get();
        } else {
            // 3. DB에 없으면, 새로 Cafe 엔티티를 만들어 저장합니다.
            Cafe newCafe = Cafe.builder()
                    .kakaoPlaceId(kakaoPlaceId)
                    .name((String) kakaoCafeInfo.get("place_name"))
                    .address((String) kakaoCafeInfo.get("road_address_name"))
                    .phoneNumber((String) kakaoCafeInfo.get("phone"))
                    .build();
            cafe = cafeRepository.save(newCafe);
        }

        // 4. 최종 카페 정보와 채팅방 목록을 합쳐서 반환합니다.
        List<ChatRoomResponse> chatRooms = chatRoomService.getRoomsByCafe(cafe.getId());
        return new CafeWithChatRoomsResponse(cafe, chatRooms);
    }

    private Map<String, Object> callKakaoPlaceSearchApi(String query) {
        String url = "https://dapi.kakao.com/v2/local/search/keyword.json?query=" + query;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoApiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

        List<Map<String, Object>> documents = (List<Map<String, Object>>) response.getBody().get("documents");
        if (documents == null || documents.isEmpty()) {
            throw new RuntimeException("Could not find place info from Kakao API for query: " + query);
        }
        return documents.get(0);
    }
}
