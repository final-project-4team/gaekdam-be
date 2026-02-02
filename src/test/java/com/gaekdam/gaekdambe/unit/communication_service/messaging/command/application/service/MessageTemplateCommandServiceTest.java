package com.gaekdam.gaekdambe.unit.communication_service.messaging.command.application.service;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import com.gaekdam.gaekdambe.communication_service.messaging.command.application.dto.request.MessageTemplateCreateRequest;
import com.gaekdam.gaekdambe.communication_service.messaging.command.application.dto.request.MessageTemplateUpdateRequest;
import com.gaekdam.gaekdambe.communication_service.messaging.command.application.service.MessageTemplateCommandService;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.entity.MessageTemplate;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.enums.LanguageCode;
import com.gaekdam.gaekdambe.communication_service.messaging.command.infrastructure.repository.MessageTemplateRepository;

class MessageTemplateCommandServiceTest {

    @Mock
    MessageTemplateRepository repository;

    private MessageTemplateCommandService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new MessageTemplateCommandService(repository);
    }

    @Test
    void createTemplate_returnsTemplateCode() {
        MessageTemplateCreateRequest req = org.mockito.Mockito.mock(MessageTemplateCreateRequest.class);
        when(req.getVisitorType()).thenReturn(null);
        when(req.getLanguageCode()).thenReturn(LanguageCode.KOR);
        when(req.getTitle()).thenReturn("t");
        when(req.getContent()).thenReturn("c");
        when(req.getConditionExpr()).thenReturn(null);
        when(req.isActive()).thenReturn(true);
        when(req.getStageCode()).thenReturn(11L);

        org.mockito.Mockito.when(repository.save(org.mockito.ArgumentMatchers.any(MessageTemplate.class)))
                .thenAnswer(invocation -> {
                    MessageTemplate t = invocation.getArgument(0);
                    try {
                        java.lang.reflect.Field f = MessageTemplate.class.getDeclaredField("templateCode");
                        f.setAccessible(true);
                        f.set(t, 333L);
                    } catch (Exception e) {
                        // ignore
                    }
                    return t;
                });

        Long code = service.createTemplate(req, 22L);
        assertThat(code).isEqualTo(333L);
    }

    @Test
    void update_throwsWhenNotFound() {
        MessageTemplateUpdateRequest req = org.mockito.Mockito.mock(MessageTemplateUpdateRequest.class);
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Template not found");
    }

    @Test
    void update_appliesChanges() {
        MessageTemplate existing = MessageTemplate.builder()
                .templateCode(10L)
                .visitorType(null)
                .languageCode(LanguageCode.ENG)
                .title("old")
                .content("oldc")
                .conditionExpr(null)
                .isActive(true)
                .hotelGroupCode(1L)
                .stageCode(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(repository.findById(10L)).thenReturn(Optional.of(existing));

        MessageTemplateUpdateRequest req = org.mockito.Mockito.mock(MessageTemplateUpdateRequest.class);
        when(req.getTitle()).thenReturn("newt");
        when(req.getContent()).thenReturn("newc");
        when(req.getLanguageCode()).thenReturn(LanguageCode.KOR);
        when(req.getIsActive()).thenReturn(false);
        when(req.getConditionExpr()).thenReturn("x>1");

        service.update(10L, req);

        assertThat(existing.getTitle()).isEqualTo("newt");
        assertThat(existing.getContent()).isEqualTo("newc");
        assertThat(existing.getLanguageCode()).isEqualTo(LanguageCode.KOR);
        assertThat(existing.isActive()).isFalse();
        assertThat(existing.getConditionExpr()).isEqualTo("x>1");
    }
}
