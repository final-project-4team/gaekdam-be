package com.gaekdam.gaekdambe.customer_service.membership.query.mapper;

import com.gaekdam.gaekdambe.customer_service.membership.command.domain.entity.MembershipGrade;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface MembershipBatchMapper {

    // 오늘 산정해야 할 호텔 그룹의 등급 정책 목록 조회
    List<MembershipGrade> selectTargetGradePolicies(@Param("dayOfMonth") int dayOfMonth);

    // 고객의 특정 기간 내 실적 조회 (총 금액, 방문 횟수)
    // Map returns: "totalAmount" (BigDecimal), "visitCount" (Long)
    Map<String, Object> selectCustomerStatistics(
            @Param("customerCode") Long customerCode,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
