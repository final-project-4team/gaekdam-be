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

    @Column(name = "type_name", nullable = false, length = 50)
    private String typeName;

    @Column(name = "max_capacity", nullable = false)
    private int maxCapacity;

    @Column(name = "bed_type", nullable = false, length = 50)
    private String bedType;

    @Column(name = "view_type", length = 30)
    private String viewType;

    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "description")
    private String description;

    @Column(name = "property_code", nullable = false)
    private Long propertyCode;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    public static RoomType createRoomType(
            String name,
            int capacity,
            String bedType,
            String viewType,
            BigDecimal price,
            String description,
            Long propertyCode
    ) {
        LocalDateTime now = LocalDateTime.now();

        return RoomType.builder()
                .typeName(name)
                .maxCapacity(capacity)
                .bedType(bedType)
                .viewType(viewType)
                .basePrice(price)
                .description(description)
                .propertyCode(propertyCode)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}