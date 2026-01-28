package com.gaekdam.gaekdambe.reservation_service.reservation.command.application.service;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.event.MessageJourneyEvent;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.resolver.MessageStageResolver;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.application.dto.request.ReservationCreateRequest;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.domain.entity.Reservation;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.domain.enums.ReservationStatus;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.infrastructure.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationCreateCommandService {

    private final ReservationRepository reservationRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final MessageStageResolver stageResolver;

    /**
     * 예약 등록 = 예약 확정
     */
    public Long create(ReservationCreateRequest req) {

        Reservation reservation =
                Reservation.createReservation(
                        req.getCheckinDate(),
                        req.getCheckoutDate(),
                        req.getGuestCount(),
                        req.getGuestType(),
                        req.getReservationChannel(),
                        req.getReservationRoomPrice(),
                        req.getReservationPackagePrice(),
                        req.getPropertyCode(),
                        req.getRoomCode(),
                        req.getCustomerCode(),
                        req.getPackageCode(),
                        ReservationStatus.RESERVED   // 등록 즉시 확정
                );

        reservationRepository.save(reservation);

        // 예약 확정 메시지 이벤트
        Long stageCode =
                stageResolver.resolveStageCode("RESERVATION_CONFIRMED");

        eventPublisher.publishEvent(
                new MessageJourneyEvent(
                        stageCode,
                        reservation.getReservationCode(),
                        null
                )
        );

        return reservation.getReservationCode();
    }
}
