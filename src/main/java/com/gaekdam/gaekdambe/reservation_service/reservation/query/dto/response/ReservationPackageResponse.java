package com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationPackageResponse {
    private String packageName;
    private String packageContent;
}
