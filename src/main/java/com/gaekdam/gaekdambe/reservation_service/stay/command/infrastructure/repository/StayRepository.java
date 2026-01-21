package com.gaekdam.gaekdambe.reservation_service.stay.command.infrastructure.repository;

import com.gaekdam.gaekdambe.reservation_service.reservation.command.domain.entity.Reservation;
import com.gaekdam.gaekdambe.reservation_service.stay.command.domain.entity.Stay;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StayRepository extends JpaRepository<Stay,Long> {
    Optional<Object> findByReservationCode(Long reservationCode);
}
