package com.gaekdam.gaekdambe.communication_service.messaging.scheduler;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.event.MessageJourneyEvent;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.resolver.MessageStageResolver;
import com.gaekdam.gaekdambe.communication_service.messaging.query.mapper.MessagingJourneyTargetQueryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Profile("!test")
public class MessageJourneyStatusScheduler {

    private final MessagingJourneyTargetQueryMapper mapper;
    private final MessageStageResolver stageResolver;
    private final ApplicationEventPublisher publisher;

    // 1분 주기
    @Scheduled(cron = "0 */1 * * * *")
    public void run() {

//        예약확정은 우선 command로 직접들어온다가정
//        publishReservationConfirmed();

        publishReservationCancelled();
        publishNoShow();
        publishCheckInConfirmed();
        publishCheckOutConfirmed();
    }

    private void publishReservationConfirmed() {
        Long stageCode = stageResolver.resolveStageCode("RESERVATION_CONFIRMED");
        List<Long> targets = mapper.findReservationConfirmedTargets(stageCode);

        targets.forEach(code ->
                publisher.publishEvent(new MessageJourneyEvent(stageCode, code, null))
        );
    }

    private void publishReservationCancelled() {
        Long stageCode = stageResolver.resolveStageCode("RESERVATION_CANCELLED");
        mapper.findReservationCancelledTargets(stageCode)
                .forEach(code ->
                        publisher.publishEvent(new MessageJourneyEvent(stageCode, code, null))
                );
    }

    private void publishNoShow() {
        Long stageCode = stageResolver.resolveStageCode("NOSHOW_CONFIRMED");
        mapper.findNoShowTargets(stageCode)
                .forEach(code ->
                        publisher.publishEvent(new MessageJourneyEvent(stageCode, code, null))
                );
    }

    private void publishCheckInConfirmed() {
        Long stageCode = stageResolver.resolveStageCode("CHECKIN_CONFIRMED");
        mapper.findCheckInConfirmedStayTargets(stageCode)
                .forEach(code ->
                        publisher.publishEvent(new MessageJourneyEvent(stageCode, null, code))
                );
    }

    private void publishCheckOutConfirmed() {
        Long stageCode = stageResolver.resolveStageCode("CHECKOUT_CONFIRMED");
        mapper.findCheckOutConfirmedStayTargets(stageCode)
                .forEach(code ->
                        publisher.publishEvent(new MessageJourneyEvent(stageCode, null, code))
                );
    }
}
