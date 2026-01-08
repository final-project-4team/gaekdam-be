package com.gaekdam.gaekdambe.hotel_service.hotel.command.infrastructure.repository;

import com.gaekdam.gaekdambe.hotel_service.hotel.command.domain.entity.HotelGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HotelGroupRepository extends JpaRepository<HotelGroup,Long> {

}
