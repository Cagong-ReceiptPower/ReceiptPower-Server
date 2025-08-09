package com.cagong.receiptpowerserver.domain.mileage;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mileage")
@RequiredArgsConstructor
public class MileageController {

    private final MileageService mileageService;

    /**
     * 마일리지 적립 API
     * POST /api/mileage/add
     * 요청 바디: { "memberId": 1, "point": 100 }
     */
    @PostMapping("/add")
    public ResponseEntity<String> addMileage(@RequestBody MileageRequestDto requestDto) {
        mileageService.saveMileage(requestDto.getMemberId(), requestDto.getPoint());
        return ResponseEntity.ok("마일리지 적립 성공");
    }

    /**
     * 마일리지 사용(차감) API
     * POST /api/mileage/use
     * 요청 바디: { "memberId": 1, "point": 50 }
     */
    @PostMapping("/use")
    public ResponseEntity<String> useMileage(@RequestBody MileageRequestDto requestDto) {
        mileageService.useMileage(requestDto.getMemberId(), requestDto.getPoint());
        return ResponseEntity.ok("마일리지 사용 완료");
    }

    /**
     * 회원별 마일리지 잔액 조회 API
     * GET /api/mileage/balance/{memberId}
     */
    @GetMapping("/balance/{memberId}")
    public ResponseEntity<Integer> getMileageBalance(@PathVariable Long memberId) {
        Integer totalPoints = mileageService.getTotalMileage(memberId);
        return ResponseEntity.ok(totalPoints == null ? 0 : totalPoints);
    }
}
