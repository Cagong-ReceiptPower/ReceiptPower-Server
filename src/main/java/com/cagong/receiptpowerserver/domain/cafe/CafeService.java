package com.cagong.receiptpowerserver.domain.cafe;

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
}
