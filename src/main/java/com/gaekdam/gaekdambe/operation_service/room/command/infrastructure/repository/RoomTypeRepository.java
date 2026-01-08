package com.gaekdam.gaekdambe.operation_service.room.command.infrastructure.repository;

import com.gaekdam.gaekdambe.operation_service.room.command.domain.entity.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomTypeRepository extends JpaRepository<RoomType,Long> {
}
