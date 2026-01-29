package com.gaekdam.gaekdambe.iam_service.log.query.service;

import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.iam_service.log.query.dto.request.AuditLogSearchRequest;
import com.gaekdam.gaekdambe.iam_service.log.query.dto.response.AuditLogQueryResponse;
import com.gaekdam.gaekdambe.iam_service.log.query.mapper.AuditLogMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditLogQueryService {
    private final AuditLogMapper auditLogMapper;

    public PageResponse<AuditLogQueryResponse> getAuditLogs(
            Long hotelGroupCode,
            PageRequest page,
            AuditLogSearchRequest search,
            SortRequest sort) {

        List<AuditLogQueryResponse> list = auditLogMapper.findAuditLogs(hotelGroupCode, page, search, sort);
        long total = auditLogMapper.countAuditLogs(hotelGroupCode, search);

        return new PageResponse<>(
                list,
                page.getPage(),
                page.getSize(),
                total);
    }

    public AuditLogQueryResponse getAuditLog(Long auditLogCode) {
        return auditLogMapper.findAuditLog(auditLogCode)
                .orElseThrow(() -> new IllegalArgumentException("Audit Log not found with id: " + auditLogCode));
    }
}
