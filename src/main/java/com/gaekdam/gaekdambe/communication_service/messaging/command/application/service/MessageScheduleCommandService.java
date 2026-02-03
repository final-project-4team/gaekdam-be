package com.gaekdam.gaekdambe.communication_service.messaging.command.application.service;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageRule;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.VisitorType;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.event.MessageJourneyEvent;
import com.gaekdam.gaekdambe.communication_service.messaging.query.mapper.MessageRuleQueryMapper;
import com.gaekdam.gaekdambe.communication_service.messaging.query.mapper.MessagingVisitorTypeQueryMapper;
import com.gaekdam.gaekdambe.reservation_service.stay.command.domain.entity.Stay;
import com.gaekdam.gaekdambe.reservation_service.stay.command.infrastructure.repository.StayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageScheduleCommandService {

    private final MessageRuleQueryMapper ruleQueryMapper;
    private final HistorySaveService historySaveService;

    private final StayRepository stayRepository;
    private final MessagingVisitorTypeQueryMapper visitorTypeQueryMapper;
    private final JdbcTemplate jdbcTemplate;

    public void schedule(MessageJourneyEvent event) {

        Long reservationCode = event.getReservationCode();
        Long stayCode = event.getStayCode();

        // stay 기반 이벤트 → reservationCode 보정
        if (reservationCode == null && stayCode != null) {
            Stay stay = stayRepository.findById(stayCode).orElseThrow();
            reservationCode = stay.getReservationCode();
        }

        // 1 hotel_group_code 결정
        Long hotelGroupCode =
                jdbcTemplate.queryForObject(
                        "SELECT p.hotel_group_code FROM reservation r JOIN property p ON r.property_code = p.property_code WHERE r.reservation_code = ?",
                        Long.class,
                        reservationCode
                );

        // 2 방문자 타입 판정
        VisitorType visitorType =
                visitorTypeQueryMapper.resolveVisitorType(reservationCode);


        System.out.println("[DEBUG] schedule start");
        System.out.println("[DEBUG] reservationCode = " + reservationCode);
        System.out.println("[DEBUG] stageCode = " + event.getStageCode());
        System.out.println("[DEBUG] hotelGroupCode = " + hotelGroupCode);
        System.out.println("[DEBUG] visitorType = " + visitorType);

        // 3 룰 조회
        List<MessageRule> rules =
                ruleQueryMapper.findActiveRulesForSchedule(
                        hotelGroupCode,
                        event.getStageCode(),
                        visitorType
                );

        // 핵심 로그
        System.out.println("[DEBUG] rules.size = " + rules.size());

        for (MessageRule rule : rules) {
            System.out.println(
                    "[DEBUG] ruleCode=" + rule.getRuleCode()
                            + ", templateCode=" + rule.getTemplateCode()
                            + ", visitorType=" + rule.getVisitorType()
            );
        }

        // 4 히스토리 생성
        for (MessageRule rule : rules) {
            historySaveService.saveHistory(event, rule);
        }
    }
}
