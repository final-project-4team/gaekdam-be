package com.gaekdam.gaekdambe.customer_service.membership.command.infrastructure.repository;

import com.gaekdam.gaekdambe.customer_service.membership.command.domain.entity.MembershipGrade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipGradeRepository extends JpaRepository<MembershipGrade, Long> {
    java.util.List<MembershipGrade> findAllByHotelGroup_HotelGroupCode(Long hotelGroupCode);
}
