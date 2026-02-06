package com.gaekdam.gaekdambe.reservation_service.stay.command.infrastructure.repository;

import com.gaekdam.gaekdambe.reservation_service.stay.command.domain.entity.CheckInOut;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckInOutRepository extends JpaRepository<CheckInOut,Long> {
}
