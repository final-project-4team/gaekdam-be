package com.gaekdam.gaekdambe.hotel_service.hotel.command.infrastructure.repository;

import com.gaekdam.gaekdambe.hotel_service.hotel.command.domain.entity.HotelGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HotelGroupRepository extends JpaRepository<HotelGroup,Long> {

    @Query("select hg.hotelGroupCode from HotelGroup hg")
    List<Long> findAllHotelGroupCodes();

}
