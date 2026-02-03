package com.gaekdam.gaekdambe.iam_service.log.query.mapper;

import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.iam_service.log.query.dto.request.AuditLogSearchRequest;
import com.gaekdam.gaekdambe.iam_service.log.query.dto.response.AuditLogQueryResponse;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AuditLogMapper {

    List<AuditLogQueryResponse> findAuditLogs(
            @Param("hotelGroupCode") Long hotelGroupCode,
            @Param("page") PageRequest page,
            @Param("search") AuditLogSearchRequest search,
            @Param("sort") SortRequest sort);

    long countAuditLogs(
            @Param("hotelGroupCode") Long hotelGroupCode,
            @Param("search") AuditLogSearchRequest search);

    Optional<AuditLogQueryResponse> findAuditLog(Long auditLogCode);
}
