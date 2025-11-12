package com.cagong.receiptpowerserver.domain.mileage;

import com.cagong.receiptpowerserver.domain.mileage.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mileages")
public class MileageController {

    private final MileageService mileageService;

    @GetMapping("/total")
    @Operation(summary = "전체 마일리지 조회")
    public ResponseEntity<TotalMileageResponse> getTotalMileage(){
        try {
            TotalMileageResponse response = mileageService.getTotalMileage();
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    @Operation(summary = "개별 마일리지 조회")
    public ResponseEntity<CafeMileageResponse> getCafeMileage(@RequestParam Long cafeId){
        try {
            CafeMileageResponse response = mileageService.getCafeMileage(cafeId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    @Operation(summary = "마일리지 적립")
    public ResponseEntity<SaveMileageResponse> saveMileage(@RequestBody SaveMileageRequest request){
        try {
            SaveMileageResponse response = mileageService.addMileage(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{cafeId}/usage")
    @Operation(summary = "마일리지 사용 시작")
    public ResponseEntity<UseMileageResponse> useMileage(@PathVariable Long cafeId){
        try {
            UseMileageResponse response = mileageService.startMileageUsage(cafeId);
            return ResponseEntity.ok().body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{cafeId}/usage/end")
    @Operation(summary = "마일리지 사용 끝")
    public ResponseEntity<EndMileageUsageResponse> endMileage(@PathVariable Long cafeId){
        try {
            EndMileageUsageResponse response = mileageService.endMileageUsage(cafeId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
