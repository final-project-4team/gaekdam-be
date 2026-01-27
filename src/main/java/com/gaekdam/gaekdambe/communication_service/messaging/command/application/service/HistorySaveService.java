package com.gaekdam.gaekdambe.communication_service.messaging.command.application.service;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageRule;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageSendHistory;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageTemplate;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.MessageSendStatus;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.event.MessageJourneyEvent;
import com.gaekdam.gaekdambe.communication_service.messaging.command.infrastructure.repository.MessageSendHistoryRepository;
import com.gaekdam.gaekdambe.communication_service.messaging.command.infrastructure.repository.MessageTemplateRepository;
import com.gaekdam.gaekdambe.communication_service.messaging.query.dto.response.MessagingConditionContext;
import com.gaekdam.gaekdambe.communication_service.messaging.query.mapper.MessagingConditionContextQueryMapper;
import com.gaekdam.gaekdambe.reservation_service.stay.command.domain.entity.Stay;
import com.gaekdam.gaekdambe.reservation_service.stay.command.infrastructure.repository.StayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * MessageSendHistory 단건 저장
     * - 중복 발생 시 이 트랜잭션만 롤백
     * - 컨텍스트 누락 / 조건 실패는 SKIPPED 로 기록
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveHistory(MessageJourneyEvent event, MessageRule rule) {

        Long reservationCode = event.getReservationCode();
        Long stayCode = event.getStayCode();

        // stay 기반 이벤트는 reservationCode 보정
        if (reservationCode == null && stayCode != null) {
            Stay stay = stayRepository.findById(stayCode).orElseThrow();
            reservationCode = stay.getReservationCode();
        }

        MessageTemplate template = templateRepository.findById(rule.getTemplateCode())
                .orElseThrow(() ->
                        new IllegalArgumentException("Template not found: " + rule.getTemplateCode())
                );

        LocalDateTime scheduledAt = LocalDateTime.now().plusMinutes(rule.getOffsetMinutes());

        // 템플릿 비활성 → SKIPPED
        if (!template.isActive()) {
            saveSkipped(event, rule, reservationCode, stayCode, scheduledAt, "template inactive");
            return;
        }

        // conditionExpr 없으면 조건 검사 없이 통과
        if (template.getConditionExpr() != null) {

            // conditionExpr 평가용 컨텍스트 조회 (MyBatis)
            MessagingConditionContext ctx =
                    (stayCode != null)
                            ? contextQueryMapper.findByStayCode(stayCode)
                            : contextQueryMapper.findByReservationCode(reservationCode);

            // 컨텍스트 없음 → SKIPPED
            if (ctx == null) {
                saveSkipped(event, rule, reservationCode, stayCode, scheduledAt, "condition_context_missing");
                return;
            }

            Map<String, Object> vars = new HashMap<>();
            vars.put("reservationCode", ctx.getReservationCode());
            vars.put("stayCode", ctx.getStayCode());
            vars.put("customerCode", ctx.getCustomerCode());
            vars.put("guestCount", ctx.getGuestCount());
            vars.put("membershipGradeCode", ctx.getMembershipGradeCode());
            vars.put("propertyCode", ctx.getPropertyCode());
            vars.put("reservationStatus", ctx.getReservationStatus());
            vars.put("checkinDate", ctx.getCheckinDate());
            vars.put("checkoutDate", ctx.getCheckoutDate());
            vars.put("actualCheckinAt", ctx.getActualCheckinAt());
            vars.put("actualCheckoutAt", ctx.getActualCheckoutAt());

            boolean ok = conditionExprEvaluator.evaluate(template.getConditionExpr(), vars);

            if (!ok) {
                saveSkipped(event, rule, reservationCode, stayCode, scheduledAt, "condition_expr=false");
                return;
            }
        }

        // 정상 SCHEDULED 저장
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
            // 중복 → 정상 스킵 (idempotent)
        }
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
            // 중복 → 정상 스킵
        }
    }
}
