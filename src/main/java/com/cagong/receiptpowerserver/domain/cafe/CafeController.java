
// domain/cafe/CafeController.java

package com.cagong.receiptpowerserver.domain.cafe;

import com.cagong.receiptpowerserver.domain.cafe.dto.CafeWithChatRoomsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cafes")
@RequiredArgsConstructor
public class CafeController {

    private final CafeService cafeService;

    // [수정] kakaoPlaceId 대신 query(검색어)를 받도록 변경
    @GetMapping("/details")
    public ResponseEntity<CafeWithChatRoomsResponse> getCafeDetailsByQuery(
            @RequestParam String query
    ) {
        // 서비스 메서드 이름도 변경
        CafeWithChatRoomsResponse response = cafeService.findOrCreateCafeByQueryAndGetDetails(query);
        return ResponseEntity.ok(response);
    }
}