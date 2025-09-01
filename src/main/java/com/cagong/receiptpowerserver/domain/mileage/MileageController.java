package com.cagong.receiptpowerserver.domain.mileage;

import com.cagong.receiptpowerserver.domain.mileage.dto.CafeMileageResponse;
import com.cagong.receiptpowerserver.domain.mileage.dto.EndMileageUsageResponse;
import com.cagong.receiptpowerserver.domain.mileage.dto.SaveMileageRequest;
import com.cagong.receiptpowerserver.domain.mileage.dto.TotalMileageResponse;
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
        TotalMileageResponse response = mileageService.getTotalMileage();
        return ResponseEntity.ok().body(response);
    }

    @GetMapping
    public ResponseEntity<CafeMileageResponse> getCafeMileage(@RequestParam Long cafeId){
        CafeMileageResponse response = mileageService.getCafeMileage(cafeId);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping
    public ResponseEntity<Void> saveMileage(@RequestBody SaveMileageRequest request){
        mileageService.addMileage(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/usage")
    public ResponseEntity<Void> useMileage(@RequestParam Long cafeId){
        mileageService.startMileageUsage(cafeId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("usage/end")
    public ResponseEntity<EndMileageUsageResponse> endMileage(@RequestParam Long cafeId){
        EndMileageUsageResponse response = mileageService.endMileageUsage(cafeId);
        return ResponseEntity.ok().body(response);
    }
}
