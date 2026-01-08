package com.gaekdam.gaekdambe.operation_service.facility.command.domain.entity;

import com.gaekdam.gaekdambe.reservation_service.reservation.command.domain.entity.Reservation;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "facility")
public class Facility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "facility_code")
    private Long facilityCode;

    @Column(name = "facility_name")
    private String facilityName;

    @Column(name = "facility_type")
    private String facilityType;

    @Column(name = "operating_hours")
    private String operatingHours;

    @Column(name = "operating_status")
    private String operatingStatus;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    // 생성 메서드 (예약 생성)
    public static Facility createFacility(
            String facilityName ,
            String facilityType ,
            String operatingHours ,
            String operatingStatus
    ) {
        LocalDateTime now = LocalDateTime.now();

        return Facility.builder()
                .facilityName(facilityName)
                .facilityType(facilityType)
                .operatingHours(operatingHours)
                .operatingStatus(operatingStatus != null ? operatingStatus : "ACTIVE")
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
