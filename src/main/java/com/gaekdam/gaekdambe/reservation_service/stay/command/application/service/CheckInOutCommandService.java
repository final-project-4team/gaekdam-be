package com.gaekdam.gaekdambe.reservation_service.stay.command.application.service;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.event.MessageJourneyEvent;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.resolver.MessageStageResolver;
import com.gaekdam.gaekdambe.iam_service.log.command.application.aop.annotation.AuditLog;
import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.seeds.PermissionTypeKey;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.domain.entity.Reservation;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.infrastructure.repository.ReservationRepository;
import com.gaekdam.gaekdambe.reservation_service.stay.command.application.dto.request.CheckInRequest;
import com.gaekdam.gaekdambe.reservation_service.stay.command.application.dto.request.CheckOutRequest;
import com.gaekdam.gaekdambe.reservation_service.stay.command.domain.entity.CheckInOut;
import com.gaekdam.gaekdambe.reservation_service.stay.command.domain.entity.Stay;
import com.gaekdam.gaekdambe.reservation_service.stay.command.domain.enums.CheckInOutRecordType;
import com.gaekdam.gaekdambe.reservation_service.stay.command.domain.enums.StayStatus;
import com.gaekdam.gaekdambe.reservation_service.stay.command.infrastructure.repository.CheckInOutRepository;
import com.gaekdam.gaekdambe.reservation_service.stay.command.infrastructure.repository.StayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class CheckInOutCommandService {

    private final CheckInOutRepository checkInOutRepository;
    private final StayRepository stayRepository;
    private final ReservationRepository reservationRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final MessageStageResolver stageResolver;

    /**
     * 체크인
     * - Reservation → Stay 생성
     * - CheckInOut 기록
     * - 메시징 이벤트 발행
     */
    @Transactional
    @AuditLog(details = "'체크 인   예약 코드 : ' + #request.reservationCode", type = PermissionTypeKey.CHECK_IN_CREATE)
    public void checkIn(CheckInRequest request) {

        Reservation reservation = reservationRepository.findById(request.getReservationCode())
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

        stayRepository.findByReservationCode(reservation.getReservationCode())
                .ifPresent(s -> {
                    throw new IllegalStateException("Already checked in");
                });

        Stay stay = Stay.createStay(
                reservation.getReservationCode(),
                reservation.getRoomCode(),
                reservation.getCustomerCode(),
                request.getGuestCount(),
                LocalDateTime.now(),
                null,
                StayStatus.STAYING
        );
        stayRepository.save(stay);

        CheckInOut checkIn = CheckInOut.createCheckInOut(
                CheckInOutRecordType.CHECK_IN,
                LocalDateTime.now(),
                request.getGuestCount(),
                request.getRecordChannel(),
                request.getSettlementYn(),
                stay.getStayCode()
        );
        checkInOutRepository.save(checkIn);

        // stage_code는 resolver가 책임
        Long stageCode =
                stageResolver.resolveStageCode("CHECKIN_CONFIRMED");

        eventPublisher.publishEvent(
                new MessageJourneyEvent(
                        stageCode,
                        reservation.getReservationCode(),
                        stay.getStayCode()
                )
        );
    }

    /**
     * 체크아웃
     * - Stay 종료
     * - CheckInOut 기록
     * - 메시징 이벤트 발행
     */
    @AuditLog(details = "'체크아웃   숙박 코드 : ' + #request.stayCode", type = PermissionTypeKey.CHECK_OUT_CREATE)
    public void checkOut(CheckOutRequest request) {

        Stay stay = stayRepository.findById(request.getStayCode())
                .orElseThrow(() -> new IllegalArgumentException("Stay not found"));

        if (stay.getActualCheckoutAt() != null) {
            throw new IllegalStateException("Already checked out");
        }

        LocalDateTime now = LocalDateTime.now();

        CheckInOut checkOut = CheckInOut.createCheckInOut(
                CheckInOutRecordType.CHECK_OUT,
                now,
                stay.getGuestCount(),
                request.getRecordChannel(),
                request.getSettlementYn(),
                stay.getStayCode()
        );
        checkInOutRepository.save(checkOut);

        stay.checkOut(now);

        Long stageCode =
                stageResolver.resolveStageCode("CHECKOUT_CONFIRMED");

        eventPublisher.publishEvent(
                new MessageJourneyEvent(
                        stageCode,
                        null,
                        stay.getStayCode()
                )
        );
    }
}
