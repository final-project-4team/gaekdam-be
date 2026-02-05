package com.gaekdam.gaekdambe.customer_service.insight.query.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface CustomerInsightMapper {

    // KPI 데이터 조회 (총 지출, 총 투숙일, 예약 건수, 최근 방문일)
    Map<String, Object> selectCustomerKpi(@Param("customerCode") Long customerCode);

    // 작년 총 지출 조회 (트렌드 계산용)
    BigDecimal selectLastYearTotalSpending(@Param("customerCode") Long customerCode);

    // 월별 지출 현황 (최근 12개월)
    List<Map<String, Object>> selectMonthlySpending(@Param("customerCode") Long customerCode);

    // 지출 카테고리 조회 (객실 vs 식음료/패키지)
    Map<String, BigDecimal> selectSpendingCategory(@Param("customerCode") Long customerCode);

    // 요일별 투숙 패턴 조회
    List<Map<String, Object>> selectStayDayPattern(@Param("customerCode") Long customerCode);

    // 최근 예약 내역 조회 (마케팅 인사이트 분석용, 예: 여름 시즌 여부 등)
    List<Map<String, Object>> selectRecentReservations(@Param("customerCode") Long customerCode);

    // 선호 객실 타입 조회
    List<Map<String, Object>> selectPreferredRoomType(@Param("customerCode") Long customerCode);

    // 선호 부대시설 조회
    List<Map<String, Object>> selectPreferredFacility(@Param("customerCode") Long customerCode);

    // 부대시설 상세 지출 내역 조회 (타입별)
    List<Map<String, Object>> selectFacilitySpendingDetail(@Param("customerCode") Long customerCode);
}
