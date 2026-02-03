package com.gaekdam.gaekdambe.customer_service.membership.command.infrastructure.repository;

import com.gaekdam.gaekdambe.customer_service.membership.command.domain.entity.Membership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MembershipRepository extends JpaRepository<Membership, Long> {

    Optional<Membership> findByCustomerCodeAndHotelGroupCode(Long CustomerCode, Long hotelGroupCode);

    // 같은 호텔에 같은 고객에 똑같은 맴버십을 만들지 않기 위함
    boolean existsByHotelGroupCodeAndCustomerCode(Long hotelGroupCode, Long customerCode);

    java.util.List<Membership> findAllByHotelGroupCode(Long hotelGroupCode);

}