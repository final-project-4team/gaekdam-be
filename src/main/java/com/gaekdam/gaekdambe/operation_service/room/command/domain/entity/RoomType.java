package com.gaekdam.gaekdambe.operation_service.room.command.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "room_type")
public class RoomType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_type_code")
    private Long roomTypeCode;

    @Column(name = "type_name", nullable = false, length = 50 , unique = true)
    private String typeName;

    @Column(name = "max_capacity", nullable = false)
    private Integer maxCapacity;

    @Column(name = "bed_type", nullable = false, length = 50)
    private String bedType;

    @Column(name = "view_type", length = 30)
    private String viewType;

    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 생성 메서드
    public static RoomType createRoomType(
            String typeName,
            Integer maxCapacity,
            String bedType,
            String viewType,
            BigDecimal basePrice,
            String description
    ) {
        LocalDateTime now = LocalDateTime.now();

        return RoomType.builder()
                .typeName(typeName)
                .maxCapacity(maxCapacity)
                .bedType(bedType)
                .viewType(viewType)
                .basePrice(basePrice)
                .description(description)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
