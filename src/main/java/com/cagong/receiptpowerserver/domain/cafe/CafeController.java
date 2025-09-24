package com.cagong.receiptpowerserver.domain.cafe;

import com.cagong.receiptpowerserver.domain.cafe.dto.CafeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cafes")
public class CafeController {
    private final CafeService cafeService;

    @GetMapping("/all")
    public ResponseEntity<List<CafeResponse>> getAllCafes(){
        List<CafeResponse> responses = cafeService.getAllCafes();
        return ResponseEntity.ok().body(responses);
    }
}
