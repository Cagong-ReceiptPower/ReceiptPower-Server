package com.cagong.receiptpowerserver.domain.mileage;

import com.cagong.receiptpowerserver.domain.mileage.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mileages")
public class MileageController {

    private final MileageService mileageService;

    @GetMapping("/total")
    public ResponseEntity<TotalMileageResponse> getTotalMileage(){
        try {
            TotalMileageResponse response = mileageService.getTotalMileage();
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<CafeMileageResponse> getCafeMileage(@RequestParam Long cafeId){
        try {
            CafeMileageResponse response = mileageService.getCafeMileage(cafeId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    public ResponseEntity<SaveMileageResponse> saveMileage(@RequestBody SaveMileageRequest request){
        try {
            SaveMileageResponse response = mileageService.addMileage(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{cafeId}/usage")
    public ResponseEntity<Void> useMileage(@PathVariable Long cafeId){
        try {
            mileageService.startMileageUsage(cafeId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{cafeId}/usage/end")
    public ResponseEntity<EndMileageUsageResponse> endMileage(@PathVariable Long cafeId){
        try {
            EndMileageUsageResponse response = mileageService.endMileageUsage(cafeId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
