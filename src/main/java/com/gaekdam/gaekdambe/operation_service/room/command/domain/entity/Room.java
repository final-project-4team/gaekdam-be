package com.gaekdam.gaekdambe.operation_service.room.command.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "room")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_code")
    private Long roomCode;

    @Column(name = "room_number", nullable = false)
    private Integer roomNumber; // 호텔 내 유니크

    @Column(name = "floor")
    private Integer floor;

    @Column(name = "room_status", nullable = false, length = 20)
    private String roomStatus; // ACTIVE, INACTIVE

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "room_type_code", nullable = false)
    private Long roomTypeCode;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "room_type_code", nullable = false)
//    private RoomType roomType;

    // 생성 메서드
    public static Room createRoom(
            Integer roomNumber,
            Integer floor,
            Long roomTypeCode
    ) {
        LocalDateTime now = LocalDateTime.now();

        return Room.builder()
                .roomNumber(roomNumber)
                .floor(floor)
                .roomStatus("ACTIVE")
                .roomTypeCode(roomTypeCode)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}


