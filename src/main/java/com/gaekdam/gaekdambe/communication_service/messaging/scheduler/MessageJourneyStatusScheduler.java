package com.gaekdam.gaekdambe.communication_service.messaging.scheduler;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.event.MessageJourneyEvent;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.resolver.MessageStageResolver;
import com.gaekdam.gaekdambe.communication_service.messaging.query.mapper.MessagingJourneyTargetQueryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Profile("!test")
@Slf4j
public class MessageJourneyStatusScheduler {

    private final MessagingJourneyTargetQueryMapper mapper;
    private final MessageStageResolver stageResolver;
    private final ApplicationEventPublisher publisher;

    /**
     * TODO: 실제 운영에서는 hotel_group 테이블에서 loop
     * 지금은 테스트용으로 1번 호텔그룹 고정
     */
    private static final Long HOTEL_GROUP_CODE = 1L;

    // 1분 주기
    @Scheduled(cron = "0 */1 * * * *")
    public void run() {

        log.info("[MessageJourneyStatusScheduler] RUN (hotelGroup={})", HOTEL_GROUP_CODE);
        publishReservationConfirmed();
        publishReservationCancelled();
        publishNoShow();
        publishCheckInConfirmed();
        publishCheckOutConfirmed();
    }

    /** 예약 확정 (RESERVED) */
    private void publishReservationConfirmed() {
        Long stageCode = stageResolver.resolveStageCode("RESERVATION_CONFIRMED");

        mapper.findReservationConfirmedTargets(HOTEL_GROUP_CODE, stageCode)
                .forEach(reservationCode -> {
                    log.info("[RESERVATION_CONFIRMED] reservationCode={}", reservationCode);
                    publisher.publishEvent(
                            new MessageJourneyEvent(stageCode, reservationCode, null)
                    );
                });
    }


    /** 예약 취소 */
    private void publishReservationCancelled() {
        Long stageCode = stageResolver.resolveStageCode("RESERVATION_CANCELLED");

        mapper.findReservationCancelledTargets(HOTEL_GROUP_CODE, stageCode)
                .forEach(reservationCode -> {
                    log.info("[CANCELLED] reservationCode={}", reservationCode);
                    publisher.publishEvent(
                            new MessageJourneyEvent(stageCode, reservationCode, null)
                    );
                });
    }

    /** 노쇼 확정 */
    private void publishNoShow() {
        Long stageCode = stageResolver.resolveStageCode("NOSHOW_CONFIRMED");

        mapper.findNoShowTargets(HOTEL_GROUP_CODE, stageCode)
                .forEach(reservationCode -> {
                    log.info("[NOSHOW] reservationCode={}", reservationCode);
                    publisher.publishEvent(
                            new MessageJourneyEvent(stageCode, reservationCode, null)
                    );
                });
    }

    /** 체크인 등록 */
    private void publishCheckInConfirmed() {
        Long stageCode = stageResolver.resolveStageCode("CHECKIN_CONFIRMED");

        mapper.findCheckInConfirmedStayTargets(HOTEL_GROUP_CODE, stageCode)
                .forEach(stayCode -> {
                    log.info("[CHECKIN] stayCode={}", stayCode);
                    publisher.publishEvent(
                            new MessageJourneyEvent(stageCode, null, stayCode)
                    );
                });
    }

    /** 체크아웃 등록 */
    private void publishCheckOutConfirmed() {
        Long stageCode = stageResolver.resolveStageCode("CHECKOUT_CONFIRMED");

        mapper.findCheckOutConfirmedStayTargets(HOTEL_GROUP_CODE, stageCode)
                .forEach(stayCode -> {
                    log.info("[CHECKOUT] stayCode={}", stayCode);
                    publisher.publishEvent(
                            new MessageJourneyEvent(stageCode, null, stayCode)
                    );
                });
    }
}
