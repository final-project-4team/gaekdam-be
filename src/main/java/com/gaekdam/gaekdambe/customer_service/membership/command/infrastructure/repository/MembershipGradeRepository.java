package com.gaekdam.gaekdambe.customer_service.membership.command.infrastructure.repository;

import com.gaekdam.gaekdambe.customer_service.membership.command.domain.entity.MembershipGrade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MembershipGradeRepository extends JpaRepository<MembershipGrade, Long> {

    List<MembershipGrade> findAllByHotelGroup_HotelGroupCode(Long hotelGroupCode);

    Optional<MembershipGrade> findByHotelGroup_HotelGroupCodeAndGradeName(Long hotelGroupCode, String gradeName);
}
