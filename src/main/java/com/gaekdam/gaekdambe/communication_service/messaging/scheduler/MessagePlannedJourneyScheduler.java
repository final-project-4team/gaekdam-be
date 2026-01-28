package com.gaekdam.gaekdambe.communication_service.messaging.scheduler;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.event.MessageJourneyEvent;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.resolver.MessageStageResolver;
import com.gaekdam.gaekdambe.communication_service.messaging.query.mapper.MessagingPlannedJourneyTargetQueryMapper;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.infrastructure.repository.HotelGroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * 체크인/체크아웃 예정 여정 이벤트 스케줄러
 * - TodayOperation 기준과 100% 동일
 * - hotel_group 기준 전체 loop
 */
@Component
@RequiredArgsConstructor
@Profile("!test")
@Slf4j
public class MessagePlannedJourneyScheduler {

    private final HotelGroupRepository hotelGroupQueryRepository;
    private final MessagingPlannedJourneyTargetQueryMapper queryMapper;
    private final MessageStageResolver stageResolver;
    private final ApplicationEventPublisher eventPublisher;

    // 10분 주기
    @Scheduled(cron = "0 */10 * * * *")
    public void publishPlannedJourneyEvents() {

        String today = LocalDate.now().toString();

        List<Long> hotelGroupCodes =
                hotelGroupQueryRepository.findAllHotelGroupCodes();

        log.info("[MessagePlannedJourneyScheduler] RUN today={} hotelGroups={}",
                today, hotelGroupCodes);

        for (Long hotelGroupCode : hotelGroupCodes) {
            publishForHotelGroup(hotelGroupCode, today);
        }
    }

    private void publishForHotelGroup(Long hotelGroupCode, String today) {
        publishCheckinPlanned(hotelGroupCode, today);
        publishCheckoutPlanned(hotelGroupCode, today);
    }

    /** 체크인 예정 */
    private void publishCheckinPlanned(Long hotelGroupCode, String today) {

        Long stageCode = stageResolver.resolveStageCode("CHECKIN_PLANNED");

        List<Long> reservationCodes =
                queryMapper.findTodayCheckinPlannedReservationCodes(
                        hotelGroupCode,
                        today,
                        stageCode
                );

        for (Long reservationCode : reservationCodes) {
            log.info("[CHECKIN_PLANNED][{}] reservationCode={}",
                    hotelGroupCode, reservationCode);

            eventPublisher.publishEvent(
                    new MessageJourneyEvent(stageCode, reservationCode, null)
            );
        }
    }

    /** 체크아웃 예정 */
    private void publishCheckoutPlanned(Long hotelGroupCode, String today) {

        Long stageCode = stageResolver.resolveStageCode("CHECKOUT_PLANNED");

        List<Long> stayCodes =
                queryMapper.findTodayCheckoutPlannedStayCodes(
                        hotelGroupCode,
                        today,
                        stageCode
                );

        for (Long stayCode : stayCodes) {
            log.info("[CHECKOUT_PLANNED][{}] stayCode={}",
                    hotelGroupCode, stayCode);

            eventPublisher.publishEvent(
                    new MessageJourneyEvent(stageCode, null, stayCode)
            );
        }
    }
}
