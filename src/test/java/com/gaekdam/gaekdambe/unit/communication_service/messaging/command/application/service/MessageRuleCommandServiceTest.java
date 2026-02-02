package com.gaekdam.gaekdambe.unit.communication_service.messaging.command.application.service;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import com.gaekdam.gaekdambe.communication_service.messaging.command.application.dto.request.MessageRuleCreateRequest;
import com.gaekdam.gaekdambe.communication_service.messaging.command.application.dto.request.MessageRuleUpdateRequest;
import com.gaekdam.gaekdambe.communication_service.messaging.command.application.service.MessageRuleCommandService;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageRule;
import com.gaekdam.gaekdambe.communication_service.messaging.command.infrastructure.repository.MessageRuleRepository;

class MessageRuleCommandServiceTest {

    @Mock
    MessageRuleRepository repository;

    private MessageRuleCommandService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new MessageRuleCommandService(repository);
    }

    @Test
    void createRule_returnsGeneratedRuleCode() {
        // 간단한 DTO mock을 만들어서 필요한 getter들을 stub 처리합니다.
        MessageRuleCreateRequest req = org.mockito.Mockito.mock(MessageRuleCreateRequest.class);
        when(req.getHotelGroupCode()).thenReturn(11L);
        when(req.getReferenceEntityType()).thenReturn(com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.ReferenceEntityType.RESERVATION);
        when(req.getOffsetMinutes()).thenReturn(10);
        when(req.getVisitorType()).thenReturn(null);
        when(req.getChannel()).thenReturn(com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.MessageChannel.SMS);
        when(req.isEnabled()).thenReturn(true);
        when(req.getPriority()).thenReturn(1);
        when(req.getDescription()).thenReturn("desc");
        when(req.getStageCode()).thenReturn(100L);
        when(req.getTemplateCode()).thenReturn(200L);

        // repository.save 호출 시, 엔티티에 ruleCode를 reflection으로 설정해 반환하도록 모킹
        org.mockito.Mockito.when(repository.save(org.mockito.ArgumentMatchers.any(MessageRule.class)))
                .thenAnswer(invocation -> {
                    MessageRule r = invocation.getArgument(0);
                    try {
                        java.lang.reflect.Field f = MessageRule.class.getDeclaredField("ruleCode");
                        f.setAccessible(true);
                        f.set(r, 555L);
                    } catch (Exception e) {
                        // ignore
                    }
                    return r;
                });

        Long ruleCode = service.createRule(req);
        assertThat(ruleCode).isEqualTo(555L);
        verify(repository).save(org.mockito.ArgumentMatchers.any(MessageRule.class));
    }

    @Test
    void update_throwsWhenNotFound() {
        MessageRuleUpdateRequest req = org.mockito.Mockito.mock(MessageRuleUpdateRequest.class);
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(999L, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Rule not found");
    }

    @Test
    void update_appliesChanges() {
        MessageRule existing = MessageRule.builder()
                .ruleCode(10L)
                .referenceEntityType(com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.ReferenceEntityType.RESERVATION)
                .offsetMinutes(5)
                .visitorType(null)
                .channel(com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.MessageChannel.SMS)
                .isEnabled(true)
                .priority(1)
                .description("old")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .stageCode(1L)
                .templateCode(2L)
                .hotelGroupCode(11L)
                .build();

        when(repository.findById(10L)).thenReturn(Optional.of(existing));

        MessageRuleUpdateRequest req = org.mockito.Mockito.mock(MessageRuleUpdateRequest.class);
        when(req.getTemplateCode()).thenReturn(777L);
        when(req.getOffsetMinutes()).thenReturn(15);
        when(req.getVisitorType()).thenReturn(com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.VisitorType.FIRST);
        when(req.getChannel()).thenReturn(com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.MessageChannel.EMAIL);
        when(req.getIsEnabled()).thenReturn(false);
        when(req.getPriority()).thenReturn(9);
        when(req.getDescription()).thenReturn("newdesc");

        service.update(10L, req);

        assertThat(existing.getTemplateCode()).isEqualTo(777L);
        assertThat(existing.getOffsetMinutes()).isEqualTo(15);
        assertThat(existing.getVisitorType()).isEqualTo(com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.VisitorType.FIRST);
        assertThat(existing.getChannel()).isEqualTo(com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.MessageChannel.EMAIL);
        assertThat(existing.isEnabled()).isFalse();
        assertThat(existing.getPriority()).isEqualTo(9);
        assertThat(existing.getDescription()).isEqualTo("newdesc");
    }
}
