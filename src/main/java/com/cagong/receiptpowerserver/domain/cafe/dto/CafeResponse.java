package com.cagong.receiptpowerserver.domain.cafe.dto;
// Cafe import 받는걸로 추가
import com.cagong.receiptpowerserver.domain.cafe.Cafe;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor // 6개 인자를 받는 생성자 (그대로 둠)
public class CafeResponse {
    private Long cafeId;
    private String cafeName;
    private String address;
    private double latitude;
    private double longitude;
    private String phoneNumber;

    // [추가] 2. Cafe Entity를 인자로 받는 생성자를 추가합니다.
    /**
     * Cafe Entity를 CafeResponse DTO로 변환하는 생성자
     * @param cafe DB에서 조회한 Cafe Entity
     */
    public CafeResponse(Cafe cafe) {
        this.cafeId = cafe.getId();
        this.cafeName = cafe.getName();
        this.address = cafe.getAddress();
        this.latitude = cafe.getLatitude();
        this.longitude = cafe.getLongitude();
        this.phoneNumber = cafe.getPhoneNumber();
    }
}