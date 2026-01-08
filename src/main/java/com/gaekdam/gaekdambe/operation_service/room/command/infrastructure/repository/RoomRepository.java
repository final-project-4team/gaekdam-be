package com.gaekdam.gaekdambe.operation_service.room.command.infrastructure.repository;

import com.gaekdam.gaekdambe.operation_service.room.command.domain.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room,Long> {
}
