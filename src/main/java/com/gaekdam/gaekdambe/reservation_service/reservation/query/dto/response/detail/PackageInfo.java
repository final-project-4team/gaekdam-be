package com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.detail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackageInfo {

    private String packageName;
    private String packageContent;
    private BigDecimal packagePrice;

    // MyBatis에서 직접 매핑 안 함
    private List<PackageFacilityInfo> facilities;
}
