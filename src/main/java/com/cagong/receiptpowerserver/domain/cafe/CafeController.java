package com.cagong.receiptpowerserver.domain.cafe;

import com.cagong.receiptpowerserver.domain.cafe.dto.CafeRequest;
import com.cagong.receiptpowerserver.domain.cafe.dto.CafeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
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

    @GetMapping("/{cafeId}")
    public ResponseEntity<CafeResponse> getCafeById(@PathVariable Long cafeId){
        try{
            CafeResponse response = cafeService.getCafeById(cafeId);
            return ResponseEntity.ok().body(response);
        } catch (IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<Void> createCafe(@RequestBody CafeRequest request){
        Long cafeId = cafeService.createCafe(request);
        return ResponseEntity.created(URI.create("/cafes/" + cafeId)).build();
    }

    @PutMapping("/{cafeId}")
    public ResponseEntity<Void> updateCafe (@PathVariable Long cafeId, @RequestBody CafeRequest request){
        try {
            cafeService.updateCafe(cafeId, request);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("{cafeId}")
    public ResponseEntity<Void> deleteCafe(@PathVariable Long cafeId){
        try {
            cafeService.deleteCafe(cafeId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}