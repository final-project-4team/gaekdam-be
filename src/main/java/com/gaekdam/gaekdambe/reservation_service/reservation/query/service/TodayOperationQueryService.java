package com.gaekdam.gaekdambe.reservation_service.reservation.query.service;

import com.gaekdam.gaekdambe.global.crypto.DecryptionService;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.OperationBoardResponse;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.mapper.TodayOperationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TodayOperationQueryService {

    private final TodayOperationMapper mapper;
    private final DecryptionService decryptionService;

    public PageResponse<OperationBoardResponse> findTodayOperations(
            PageRequest page,
            Long hotelGroupCode,
            Long propertyCode,
            String summaryType,
            SortRequest sort
    ) {

        LocalDate today = LocalDate.now();

        // LIST
        List<OperationBoardResponse> list =
                mapper.findTodayOperations(
                                hotelGroupCode,
                                propertyCode,
                                summaryType,
                                page,
                                today,
                                sort
                        )
                        .stream()
                        .map(row -> {

                            String customerName = "(알 수 없음)";
                            if (row.getCustomerNameEnc() != null && row.getDekEnc() != null) {
                                customerName = decryptionService.decrypt(
                                        row.getCustomerCode(),
                                        row.getDekEnc(),
                                        row.getCustomerNameEnc()
                                );
                            }

                            return OperationBoardResponse.builder()
                                    .reservationCode(row.getReservationCode())
                                    .stayCode(row.getStayCode())
                                    .customerName(customerName)
                                    .roomType(row.getRoomType())
                                    .propertyName(row.getPropertyName())
                                    .plannedCheckinDate(row.getPlannedCheckinDate())
                                    .plannedCheckoutDate(row.getPlannedCheckoutDate())
                                    .operationStatus(row.getOperationStatus())
                                    .build();
                        })
                        .toList();

        // COUNT (LIST와 동일 CASE 기준)
        long total = calculateTotalBySummaryType(
                mapper.countTodayOperationsByStatus(
                        hotelGroupCode,
                        propertyCode,
                        today
                ),
                summaryType
        );

        return new PageResponse<>(
                list,
                page.getPage(),
                page.getSize(),
                total
        );
    }

    private long calculateTotalBySummaryType(
            List<java.util.Map<String, Object>> rows,
            String summaryType
    ) {
        if (summaryType == null || summaryType.equals("ALL_TODAY")) {
            return rows.stream()
                    .mapToLong(r -> ((Number) r.get("cnt")).longValue())
                    .sum();
        }

        return rows.stream()
                .filter(r -> r.get("operationStatus").equals(summaryType))
                .mapToLong(r -> ((Number) r.get("cnt")).longValue())
                .sum();
    }


    public Map<String, Long> getTodayOperationSummary(
            Long hotelGroupCode,
            Long propertyCode
    ) {
        LocalDate today = LocalDate.now();

        List<Map<String, Object>> rows =
                mapper.countTodayOperationsByStatus(
                        hotelGroupCode,
                        propertyCode,
                        today
                );

        Map<String, Long> result = new HashMap<>();

        long total = 0;

        for (Map<String, Object> row : rows) {
            String status = (String) row.get("operationStatus");
            long cnt = ((Number) row.get("cnt")).longValue();

            result.put(status, cnt);
            total += cnt;
        }

        // 프론트 편의를 위해 ALL_TODAY 계산해서 내려줌
        result.put("ALL_TODAY", total);

        return result;
    }
}