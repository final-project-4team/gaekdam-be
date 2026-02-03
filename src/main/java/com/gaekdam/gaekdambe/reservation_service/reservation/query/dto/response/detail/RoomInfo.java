package com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.detail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RoomInfo {

    private Long roomCode;
    private Integer roomNumber;
    private Integer floor;
    private String roomTypeName;
    private BigDecimal roomBasePrice;
}
