package com.gaekdam.gaekdambe.reservation_service.reservation.command.infrastructure.repository;

import com.gaekdam.gaekdambe.reservation_service.reservation.command.domain.entity.Reservation;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.domain.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation,Long> {
    List<Reservation> findByReservationStatus(ReservationStatus reservationStatus);


    @Query("""
        select r.propertyCode
        from Reservation r
        where r.reservationCode = :reservationCode
    """)
    Long findPropertyCodeByReservationCode(Long reservationCode);;

    @Query("""
        select r.propertyCode
        from Reservation r
        where r.reservationCode = :reservationCode
    """)
    Long findPackageCodeByReservationCode(Long reservationCode);
}
