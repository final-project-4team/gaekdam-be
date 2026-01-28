package com.gaekdam.gaekdambe.communication_service.messaging.scheduler;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.event.MessageJourneyEvent;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.resolver.MessageStageResolver;
import com.gaekdam.gaekdambe.communication_service.messaging.query.mapper.MessagingPlannedJourneyTargetQueryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * 체크인/체크아웃 예정 여정 이벤트 스케줄러
 */
@Component
@RequiredArgsConstructor
@Profile("!test")
public class MessagePlannedJourneyScheduler {

    private final MessagingPlannedJourneyTargetQueryMapper queryMapper;
    private final MessageStageResolver stageResolver;
    private final ApplicationEventPublisher eventPublisher;

    private static final Long HOTEL_GROUP_CODE = 1L;

    @Scheduled(cron = "0 */5 * * * *")
    public void publishPlannedJourneyEvents() {

        String today = LocalDate.now().toString();

        publishCheckinPlanned(today);
        publishCheckoutPlanned(today);
    }

    private void publishCheckinPlanned(String today) {

        Long stageCode = stageResolver.resolveStageCode("CHECKIN_PLANNED");

        List<Long> reservationCodes =
                queryMapper.findTodayCheckinPlannedReservationCodes(
                        HOTEL_GROUP_CODE,
                        today,
                        stageCode
                );

        for (Long reservationCode : reservationCodes) {
            eventPublisher.publishEvent(
                    new MessageJourneyEvent(stageCode, reservationCode, null)
            );
        }
    }

    private void publishCheckoutPlanned(String today) {

        Long stageCode = stageResolver.resolveStageCode("CHECKOUT_PLANNED");

        List<Long> stayCodes =
                queryMapper.findTodayCheckoutPlannedStayCodes(
                        HOTEL_GROUP_CODE,
                        today,
                        stageCode
                );

        for (Long stayCode : stayCodes) {
            eventPublisher.publishEvent(
                    new MessageJourneyEvent(stageCode, null, stayCode)
            );
        }
    }
}
