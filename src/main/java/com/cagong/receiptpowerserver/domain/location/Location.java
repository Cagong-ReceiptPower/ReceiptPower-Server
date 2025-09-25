package com.cagong.receiptpowerserver.domain.location;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Double latitude;    // 위도
    
    @Column(nullable = false)
    private Double longitude;   // 경도
    
    private String address;     // 주소
    
    @CreatedDate
    private LocalDateTime createdAt;

    public Location(@NotNull @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0") Double latitude, @NotNull @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0") Double longitude) {
    }
}
