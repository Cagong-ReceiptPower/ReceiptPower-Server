package com.cagong.receiptpowerserver.domain.cafe;

import com.cagong.receiptpowerserver.domain.cafe.dto.CafeRequest;
import com.cagong.receiptpowerserver.domain.cafe.dto.CafeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CafeService {
    private final CafeRepository cafeRepository;

    public List<CafeResponse> getAllCafes(){
        return cafeRepository.findAll().stream()
                .map(cafe -> new CafeResponse(
                        cafe.getId(),
                        cafe.getName(),
                        cafe.getAddress(),
                        cafe.getLatitude(),
                        cafe.getLongitude(),
                        cafe.getPhoneNumber()
                        ))
                .collect(Collectors.toList());
    }

    public CafeResponse getCafeById(Long cafeId){
        return cafeRepository.findById(cafeId)
                .map(cafe -> new CafeResponse(
                        cafe.getId(),
                        cafe.getName(),
                        cafe.getAddress(),
                        cafe.getLatitude(),
                        cafe.getLongitude(),
                        cafe.getPhoneNumber()
                ))
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 카페를 찾을 수 없습니다: " + cafeId));
    }

    @Transactional
    public Long createCafe(CafeRequest request){
        Cafe cafe = Cafe.builder()
                .name(request.getCafeName())
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .phoneNumber(request.getPhoneNumber())
                .build();
        Cafe saved = cafeRepository.save(cafe);
        return saved.getId();
    }

    @Transactional
    public void updateCafe(Long cafeId, CafeRequest request){
        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 카페를 찾을 수 없습니다: " + cafeId));

        cafe.updateFrom(request);

        cafeRepository.save(cafe);
    }

    @Transactional
    public void deleteCafe(Long cafeId){
        if(!cafeRepository.existsById(cafeId)){
            throw new IllegalArgumentException("해당 ID의 카페를 찾을 수 없습니다: " + cafeId);
        }
        cafeRepository.deleteById(cafeId);
    }
}

