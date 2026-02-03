package com.gaekdam.gaekdambe.communication_service.messaging.scheduler;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.event.MessageJourneyEvent;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.resolver.MessageStageResolver;
import com.gaekdam.gaekdambe.communication_service.messaging.query.mapper.MessagingJourneyTargetQueryMapper;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.infrastructure.repository.HotelGroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Profile("!test")
@Slf4j
public class MessageJourneyStatusScheduler {

    private final HotelGroupRepository hotelGroupQueryRepository;
    private final MessagingJourneyTargetQueryMapper mapper;
    private final MessageStageResolver stageResolver;
    private final ApplicationEventPublisher publisher;

    // 3분 주기
    @Scheduled(cron = "0 */3 * * * *")
    public void run() {

        List<Long> hotelGroupCodes =
                hotelGroupQueryRepository.findAllHotelGroupCodes();

        log.info("[MessageJourneyStatusScheduler] RUN hotelGroups={}", hotelGroupCodes);

        for (Long hotelGroupCode : hotelGroupCodes) {
            publishForHotelGroup(hotelGroupCode);
        }
    }

    private void publishForHotelGroup(Long hotelGroupCode) {

        publishReservationConfirmed(hotelGroupCode);
        publishReservationCancelled(hotelGroupCode);
        publishNoShow(hotelGroupCode);
        publishCheckInConfirmed(hotelGroupCode);
        publishCheckOutConfirmed(hotelGroupCode);
    }

    /** 예약 확정 */
    private void publishReservationConfirmed(Long hotelGroupCode) {
        Long stageCode = stageResolver.resolveStageCode("RESERVATION_CONFIRMED");

        mapper.findReservationConfirmedTargets(hotelGroupCode, stageCode)
                .forEach(reservationCode -> {
                    log.info("[CONFIRMED][{}] reservationCode={}", hotelGroupCode, reservationCode);
                    publisher.publishEvent(
                            new MessageJourneyEvent(stageCode, reservationCode, null)
                    );
                });
    }

    /** 예약 취소 */
    private void publishReservationCancelled(Long hotelGroupCode) {
        Long stageCode = stageResolver.resolveStageCode("RESERVATION_CANCELLED");

        mapper.findReservationCancelledTargets(hotelGroupCode, stageCode)
                .forEach(reservationCode -> {
                    log.info("[CANCELLED][{}] reservationCode={}", hotelGroupCode, reservationCode);
                    publisher.publishEvent(
                            new MessageJourneyEvent(stageCode, reservationCode, null)
                    );
                });
    }

    /** 노쇼 */
    private void publishNoShow(Long hotelGroupCode) {
        Long stageCode = stageResolver.resolveStageCode("NOSHOW_CONFIRMED");

        mapper.findNoShowTargets(hotelGroupCode, stageCode)
                .forEach(reservationCode -> {
                    log.info("[NOSHOW][{}] reservationCode={}", hotelGroupCode, reservationCode);
                    publisher.publishEvent(
                            new MessageJourneyEvent(stageCode, reservationCode, null)
                    );
                });
    }

    /** 체크인 등록 */
    private void publishCheckInConfirmed(Long hotelGroupCode) {
        Long stageCode = stageResolver.resolveStageCode("CHECKIN_CONFIRMED");

        mapper.findCheckInConfirmedStayTargets(hotelGroupCode, stageCode)
                .forEach(stayCode -> {
                    log.info("[CHECKIN][{}] stayCode={}", hotelGroupCode, stayCode);
                    publisher.publishEvent(
                            new MessageJourneyEvent(stageCode, null, stayCode)
                    );
                });
    }

    /** 체크아웃 등록 */
    private void publishCheckOutConfirmed(Long hotelGroupCode) {
        Long stageCode = stageResolver.resolveStageCode("CHECKOUT_CONFIRMED");

        mapper.findCheckOutConfirmedStayTargets(hotelGroupCode, stageCode)
                .forEach(stayCode -> {
                    log.info("[CHECKOUT][{}] stayCode={}", hotelGroupCode, stayCode);
                    publisher.publishEvent(
                            new MessageJourneyEvent(stageCode, null, stayCode)
                    );
                });
    }
}
