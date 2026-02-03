package com.gaekdam.gaekdambe.reservation_service.reservation.command.infrastructure.repository;

import com.gaekdam.gaekdambe.reservation_service.reservation.command.domain.entity.PackageFacility;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PackageFacilityRepository extends JpaRepository<PackageFacility, Long> {
}
