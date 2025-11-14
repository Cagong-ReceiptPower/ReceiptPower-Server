package com.cagong.receiptpowerserver.domain.cafe;

import com.cagong.receiptpowerserver.domain.cafe.dto.CafeRequest;
import com.cagong.receiptpowerserver.domain.cafe.dto.CafeResponse;
import com.cagong.receiptpowerserver.domain.cafe.dto.CafeUpdateRequest;
import com.cagong.receiptpowerserver.domain.cafe.dto.CafeWithChatRoomsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cafes")
@RequiredArgsConstructor
public class CafeController {

    private final CafeService cafeService;
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
    public ResponseEntity<List<CafeResponse>> getAllCafes() {
        List<CafeResponse> cafes = cafeService.findAllCafes();
        return ResponseEntity.ok(cafes);
    }

    /**
     * 3. 카페 ID로 1건 조회 (GET /api/cafes/{cafeId})
     */
    @GetMapping("/{cafeId}")
    public ResponseEntity<CafeResponse> getCafeById(@PathVariable Long cafeId) {
        CafeResponse cafe = cafeService.findCafeById(cafeId);
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

    @PatchMapping("/{cafeId}")
    public ResponseEntity<CafeResponse> updateCafe(
            @PathVariable Long cafeId,
            @RequestBody CafeUpdateRequest request) {

        // 서비스의 updateCafe 메서드를 호출
        CafeResponse updatedCafe = cafeService.updateCafe(cafeId, request);

        // 수정된 정보(updatedCafe)를 클라이언트에게 200 OK와 함께 반환
        return ResponseEntity.ok(updatedCafe);
    }
}