package com.cagong.receiptpowerserver.domain.cafe;

import com.cagong.receiptpowerserver.domain.cafe.dto.CafeRequest;
import com.cagong.receiptpowerserver.domain.cafe.dto.CafeWithChatRoomsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cafes") // <-- ❗️ 경로 수정
@RequiredArgsConstructor
public class CafeController {

    private final CafeService cafeService;

    // --- ❗️ [이하 4개 메서드 추가] ---

    /**
     * 1. 카페 생성 (POST /api/cafes)
     */
    @PostMapping
    public ResponseEntity<Long> createCafe(@RequestBody CafeRequest request) {
        Long cafeId = cafeService.saveCafe(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(cafeId);
    }

    /**
     * 2. 카페 전체 조회 (GET /api/cafes/all)
     */
    @GetMapping("/all")
    public ResponseEntity<List<Cafe>> getAllCafes() {
        List<Cafe> cafes = cafeService.findAllCafes();
        return ResponseEntity.ok(cafes);
    }

    /**
     * 3. 카페 ID로 1건 조회 (GET /api/cafes/{cafeId})
     */
    @GetMapping("/{cafeId}")
    public ResponseEntity<Cafe> getCafeById(@PathVariable Long cafeId) {
        Cafe cafe = cafeService.findCafeById(cafeId);
        return ResponseEntity.ok(cafe);
    }

    /**
     * 4. 카페 삭제 (DELETE /api/cafes/{cafeId})
     */
    @DeleteMapping("/{cafeId}")
    public ResponseEntity<Void> deleteCafe(@PathVariable Long cafeId) {
        cafeService.deleteCafeById(cafeId);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    // --- ❗️ [여기까지 추가] ---


    // [수정] kakaoPlaceId 대신 query(검색어)를 받도록 변경 (기존 코드)
    @GetMapping("/details")
    public ResponseEntity<CafeWithChatRoomsResponse> getCafeDetailsByQuery(
            @RequestParam String query
    ) {
        // 서비스 메서드 이름도 변경
        CafeWithChatRoomsResponse response = cafeService.findOrCreateCafeByQueryAndGetDetails(query);
        return ResponseEntity.ok(response);
    }
}