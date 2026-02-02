package com.gaekdam.gaekdambe.unit.iam_service.log.query.service;

import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.iam_service.log.query.dto.request.AuditLogSearchRequest;
import com.gaekdam.gaekdambe.iam_service.log.query.dto.response.AuditLogQueryResponse;
import com.gaekdam.gaekdambe.iam_service.log.query.mapper.AuditLogMapper;
import com.gaekdam.gaekdambe.iam_service.log.query.service.AuditLogQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AuditLogQueryServiceTest {

    @InjectMocks
    private AuditLogQueryService service;

    @Mock
    private AuditLogMapper auditLogMapper;

    @Test
    @DisplayName("getAuditLogs: 감사 로그 페이징 조회 성공")
    void getAuditLogs_success() {
        // given
        Long hgCode = 1L;
        AuditLogSearchRequest req = new AuditLogSearchRequest(null, null, null, null, null, null);

        given(auditLogMapper.findAuditLogs(eq(hgCode), any(PageRequest.class), any(AuditLogSearchRequest.class),
                any(SortRequest.class)))
                .willReturn(List.of(new AuditLogQueryResponse(1L, null, 100L, "LoginId", "EmpName", 1L, "Det", "Prev",
                        "New", null)));
        given(auditLogMapper.countAuditLogs(eq(hgCode), any(AuditLogSearchRequest.class))).willReturn(1L);

        // when
        PageResponse<AuditLogQueryResponse> response = service.getAuditLogs(hgCode, new PageRequest(), req,
                new SortRequest());

        // then
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).details()).isEqualTo("Det");
    }

    @Test
    @DisplayName("getAuditLog: 단건 조회 성공")
    void getAuditLog_success() {
        // given
        Long logCode = 1L;
        AuditLogQueryResponse response = new AuditLogQueryResponse(logCode, null, 100L, "LoginId", "EmpName", 1L, "Det",
                "Prev", "New", null);

        given(auditLogMapper.findAuditLog(logCode)).willReturn(Optional.of(response));

        // when
        AuditLogQueryResponse result = service.getAuditLog(logCode);

        // then
        assertThat(result).isNotNull();
        assertThat(result.auditLogCode()).isEqualTo(logCode);
    }

    @Test
    @DisplayName("getAuditLog: 존재하지 않는 로그 조회 시 예외 발생")
    void getAuditLog_fail_notFound() {
        // given
        Long logCode = 999L;
        given(auditLogMapper.findAuditLog(logCode)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> service.getAuditLog(logCode))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Audit Log not found");
    }
}
