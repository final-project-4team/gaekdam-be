package com.gaekdam.gaekdambe.reservation_service.reservation.query.service;

import com.gaekdam.gaekdambe.global.crypto.*;
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
    private final SearchHashService searchHashService;

    public PageResponse<OperationBoardResponse> findTodayOperations(
            PageRequest page,
            Long hotelGroupCode,
            Long propertyCode,
            String summaryType,
            String customerName,
            String reservationCode,
            SortRequest sort
    ) {
        // ✅ 오늘 범위 (부대시설과 동일 개념)
        LocalDate today = LocalDate.now();
        LocalDate startDate = today;
        LocalDate endDate = today.plusDays(1);

        String nameHashHex = null;
        if (customerName != null && !customerName.isBlank()) {
            nameHashHex = HexUtils.toHex(
                    searchHashService.nameHash(customerName)
            );
        }

        List<OperationBoardResponse> list =
                mapper.findTodayOperations(
                                hotelGroupCode,
                                propertyCode,
                                summaryType,
                                nameHashHex,
                                reservationCode,
                                page,
                                startDate,
                                endDate,
                                sort
                        )
                        .stream()
                        .map(row -> {
                            String name = "(알 수 없음)";
                            if (row.getCustomerNameEnc() != null && row.getDekEnc() != null) {
                                String decrypted = decryptionService.decrypt(
                                        row.getCustomerCode(),
                                        row.getDekEnc(),
                                        row.getCustomerNameEnc()
                                );
                                name = MaskingUtils.maskName(decrypted);
                            }

                            return OperationBoardResponse.builder()
                                    .reservationCode(row.getReservationCode())
                                    .customerCode(row.getCustomerCode())
                                    .stayCode(row.getStayCode())
                                    .customerName(name)
                                    .roomType(row.getRoomType())
                                    .propertyName(row.getPropertyName())
                                    .plannedCheckinDate(row.getPlannedCheckinDate())
                                    .plannedCheckoutDate(row.getPlannedCheckoutDate())
                                    .operationStatus(row.getOperationStatus())
                                    .build();
                        })
                        .toList();

        long total = calculateTotalBySummaryType(
                mapper.countTodayOperationsByStatus(
                        hotelGroupCode,
                        propertyCode,
                        startDate,
                        endDate
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
            List<Map<String, Object>> rows,
            String summaryType
    ) {
        Map<String, Long> map = new HashMap<>();

        for (Map<String, Object> r : rows) {
            map.put(
                    (String) r.get("operationStatus"),
                    ((Number) r.get("cnt")).longValue()
            );
        }

        // ALL_TODAY = CHECKIN_PLANNED + STAYING
        if (summaryType == null || summaryType.equals("ALL_TODAY")) {
            return map.getOrDefault("CHECKIN_PLANNED", 0L)
                    + map.getOrDefault("STAYING", 0L);
        }

        return map.getOrDefault(summaryType, 0L);
    }

    public Map<String, Long> getTodayOperationSummary(
            Long hotelGroupCode,
            Long propertyCode
    ) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today;
        LocalDate endDate = today.plusDays(1);

        List<Map<String, Object>> rows =
                mapper.countTodayOperationsByStatus(
                        hotelGroupCode,
                        propertyCode,
                        startDate,
                        endDate
                );

        Map<String, Long> result = new HashMap<>();

        for (Map<String, Object> row : rows) {
            result.put(
                    (String) row.get("operationStatus"),
                    ((Number) row.get("cnt")).longValue()
            );
        }

        long allToday =
                result.getOrDefault("CHECKIN_PLANNED", 0L)
                        + result.getOrDefault("STAYING", 0L);

        result.put("ALL_TODAY", allToday);

        return result;
    }
}
