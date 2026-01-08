package com.gaekdam.gaekdambe.reservation_service.reservation.command.infrastructure.repository;

import com.gaekdam.gaekdambe.reservation_service.reservation.command.domain.entity.ReservationPackage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationPackageRepository extends JpaRepository<ReservationPackage,Long> {
}
