package com.gaekdam.gaekdambe.hotel_service.position.command.infrastructure.repository;

import com.gaekdam.gaekdambe.hotel_service.position.command.domain.entity.HotelPosition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HotelPositionRepository extends JpaRepository<HotelPosition, Long> {

}
