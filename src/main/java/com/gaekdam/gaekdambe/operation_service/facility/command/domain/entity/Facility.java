package com.gaekdam.gaekdambe.operation_service.facility.command.domain.entity;

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

    @Column(name = "facility_name", nullable = false, length = 50)
    private String facilityName;

    @Column(name = "facility_type", nullable = false, length = 50)
    private String facilityType; // 식사 / 운동 / 휴식 / 여가 / 레저

    @Column(name = "operating_hours")
    private String operatingHours;

    @Column(name = "operating_status", nullable = false)
    private String operatingStatus;

    @Column(name = "property_code", nullable = false)
    private Long propertyCode;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 생성 메서드
    public static Facility createFacility(
            String name,
            String type,
            String hours,
            String status,
            Long propertyCode
    ) {
        LocalDateTime now = LocalDateTime.now();

        return Facility.builder()
                .facilityName(name)
                .facilityType(type)
                .operatingHours(hours)
                .operatingStatus(status != null ? status : "ACTIVE")
                .propertyCode(propertyCode)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}