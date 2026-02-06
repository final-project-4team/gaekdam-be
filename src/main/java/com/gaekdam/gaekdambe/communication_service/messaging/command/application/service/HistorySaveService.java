package com.gaekdam.gaekdambe.communication_service.messaging.command.application.service;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageRule;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageSendHistory;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageTemplate;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.MessageSendStatus;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.event.MessageJourneyEvent;
import com.gaekdam.gaekdambe.communication_service.messaging.command.infrastructure.repository.MessageJourneyStageRepository;
import com.gaekdam.gaekdambe.communication_service.messaging.command.infrastructure.repository.MessageSendHistoryRepository;
import com.gaekdam.gaekdambe.communication_service.messaging.command.infrastructure.repository.MessageTemplateRepository;
import com.gaekdam.gaekdambe.communication_service.messaging.policy.CheckinPolicyTime;
import com.gaekdam.gaekdambe.communication_service.messaging.query.dto.response.MessagingConditionContext;
import com.gaekdam.gaekdambe.communication_service.messaging.query.mapper.MessagingConditionContextQueryMapper;
import com.gaekdam.gaekdambe.reservation_service.stay.command.domain.entity.Stay;
import com.gaekdam.gaekdambe.reservation_service.stay.command.infrastructure.repository.StayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HistorySaveService {

    private final MessageSendHistoryRepository historyRepository;
    private final StayRepository stayRepository;
    private final MessageTemplateRepository templateRepository;
    private final MessagingConditionContextQueryMapper contextQueryMapper;
    private final ConditionExprEvaluator conditionExprEvaluator;
    private final MessageJourneyStageRepository stageRepository;

    /**
     * MessageSendHistory 단건 저장
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveHistory(MessageJourneyEvent event, MessageRule rule) {

        Long reservationCode = event.getReservationCode();
        Long stayCode = event.getStayCode();

        // stay 기반 이벤트 → reservationCode 보정
        if (reservationCode == null && stayCode != null) {
            Stay stay = stayRepository.findById(stayCode).orElseThrow();
            reservationCode = stay.getReservationCode();
        }

        if (rule.getTemplateCode() == null) {
            saveSkipped(event, rule, reservationCode, stayCode, LocalDateTime.now(), "template_code_null");
            return;
        }

        MessageTemplate template =
                templateRepository.findById(rule.getTemplateCode())
                        .orElseThrow(() ->
                                new IllegalArgumentException("Template not found: " + rule.getTemplateCode())
                        );

        // stage 정책 기반 scheduledAt 계산
        LocalDateTime scheduledAt =
                calculateScheduledAt(event, rule, reservationCode, stayCode);

        // 템플릿 비활성
        if (!template.isActive()) {
            saveSkipped(event, rule, reservationCode, stayCode, scheduledAt, "template_inactive");
            return;
        }

        String conditionExpr = template.getConditionExpr();

        if (conditionExpr != null && !conditionExpr.isBlank()) {

            MessagingConditionContext ctx =
                    (stayCode != null)
                            ? contextQueryMapper.findByStayCode(stayCode)
                            : contextQueryMapper.findByReservationCode(reservationCode);

            if (ctx == null) {
                saveSkipped(event, rule, reservationCode, stayCode, scheduledAt, "condition_context_missing");
                return;
            }

            Map<String, Object> vars = new HashMap<>();
            vars.put("reservationCode", ctx.getReservationCode());
            vars.put("stayCode", ctx.getStayCode());
            vars.put("customerCode", ctx.getCustomerCode());
            vars.put("guestCount", ctx.getGuestCount());
            vars.put("propertyCode", ctx.getPropertyCode());
            vars.put("reservationStatus", ctx.getReservationStatus());
            vars.put("checkinDate", ctx.getCheckinDate());
            vars.put("checkoutDate", ctx.getCheckoutDate());
            vars.put("actualCheckinAt", ctx.getActualCheckinAt());
            vars.put("actualCheckoutAt", ctx.getActualCheckoutAt());

            boolean ok;
            try {
                ok = conditionExprEvaluator.evaluate(conditionExpr, vars);
            } catch (Exception e) {
                saveSkipped(event, rule, reservationCode, stayCode, scheduledAt, "condition_expr_error");
                return;
            }

            if (!ok) {
                saveSkipped(event, rule, reservationCode, stayCode, scheduledAt, "condition_expr_false");
                return;
            }
        }

        MessageSendHistory history = MessageSendHistory.builder()
                .stageCode(event.getStageCode())
                .reservationCode(reservationCode)
                .stayCode(stayCode)
                .ruleCode(rule.getRuleCode())
                .templateCode(rule.getTemplateCode())
                .channel(rule.getChannel())
                .scheduledAt(scheduledAt)
                .status(MessageSendStatus.SCHEDULED)
                .build();

        try {
            historyRepository.save(history);
        } catch (DataIntegrityViolationException e) {
            // idempotent
        }
    }

    /**
     * stage별 scheduledAt 계산
     */
    private LocalDateTime calculateScheduledAt(
            MessageJourneyEvent event,
            MessageRule rule,
            Long reservationCode,
            Long stayCode
    ) {

        String stage =
                stageRepository.findById(event.getStageCode())
                        .orElseThrow()
                        .getStageNameEng();

        // 체크인 예정
        if ("CHECKIN_PLANNED".equals(stage)) {
            LocalDate checkinDate =
                    contextQueryMapper.findCheckinDateByReservationCode(reservationCode);

            return checkinDate
                    .atTime(CheckinPolicyTime.CHECKIN_TIME)
                    .plusMinutes(rule.getOffsetMinutes());
        }

        // 체크아웃 예정
        if ("CHECKOUT_PLANNED".equals(stage)) {
            LocalDate checkoutDate =
                    contextQueryMapper.findCheckoutDateByStayCode(stayCode);

            return checkoutDate
                    .atTime(CheckinPolicyTime.CHECKOUT_TIME)
                    .plusMinutes(rule.getOffsetMinutes());
        }

        // 나머지 → 즉시
        return LocalDateTime.now().plusMinutes(rule.getOffsetMinutes());
    }

    private void saveSkipped(
            MessageJourneyEvent event,
            MessageRule rule,
            Long reservationCode,
            Long stayCode,
            LocalDateTime scheduledAt,
            String reason
    ) {

        MessageSendHistory history = MessageSendHistory.builder()
                .stageCode(event.getStageCode())
                .reservationCode(reservationCode)
                .stayCode(stayCode)
                .ruleCode(rule.getRuleCode())
                .templateCode(rule.getTemplateCode())
                .channel(rule.getChannel())
                .scheduledAt(scheduledAt)
                .status(MessageSendStatus.SKIPPED)
                .failReason(reason)
                .build();

        try {
            historyRepository.save(history);
        } catch (DataIntegrityViolationException e) {
            // idempotent
        }
    }
}