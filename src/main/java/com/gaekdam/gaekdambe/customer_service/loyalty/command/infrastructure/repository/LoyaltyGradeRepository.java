package com.gaekdam.gaekdambe.customer_service.loyalty.command.infrastructure.repository;

import com.gaekdam.gaekdambe.customer_service.loyalty.command.domain.entity.LoyaltyGrade;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.domain.entity.HotelGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoyaltyGradeRepository extends JpaRepository<LoyaltyGrade, Long> {
    boolean existsByHotelGroupAndLoyaltyGradeName(HotelGroup hotelGroup, String loyaltyGradeName);

    java.util.List<LoyaltyGrade> findAllByHotelGroup_HotelGroupCode(Long hotelGroupCode);
}
