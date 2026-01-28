package com.gaekdam.gaekdambe.reservation_service.reservation.query.service;

import com.gaekdam.gaekdambe.global.crypto.DecryptionService;
import com.gaekdam.gaekdambe.global.crypto.MaskingUtils;
import com.gaekdam.gaekdambe.global.crypto.Normalizer;
import com.gaekdam.gaekdambe.global.crypto.SearchHashService;
import com.gaekdam.gaekdambe.global.crypto.HexUtils;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.domain.enums.ReservationStatus;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.request.OperationBoardSearchRequest;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.OperationBoardResponse;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.mapper.OperationBoardMapper;
import com.gaekdam.gaekdambe.reservation_service.stay.command.domain.enums.StayStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OperationBoardQueryService {

    private final OperationBoardMapper mapper;
    private final DecryptionService decryptionService;
    private final SearchHashService searchHashService;
    public PageResponse<OperationBoardResponse> findOperationBoard(
            PageRequest page,
            OperationBoardSearchRequest search,
            SortRequest sort
    ) {


        if (search.getKeyword() != null && search.getKeyword().isBlank()) {
            search.setKeyword(null);
        }
        // =========================
        // 고객명 → 해시 변환 (핵심)
        // =========================
        if (search.getCustomerName() != null && !search.getCustomerName().isBlank()) {
            String normalizedName = Normalizer.name(search.getCustomerName());
            String nameHashHex = HexUtils.toHex(
                    searchHashService.nameHash(normalizedName)
            );

            search.setCustomerNameHash(nameHashHex);
            search.setCustomerName(null);
        }

        // =========================
        // Summary 필터
        // =========================
        if (search.getSummaryType() != null) {
            applySummaryFilter(search);
        }

        List<OperationBoardResponse> list =
                mapper.findOperationBoard(page, search, sort)
                        .stream()
                        .map(row -> {

                            String customerName = "(알 수 없음)";
                            if (row.getCustomerNameEnc() != null && row.getDekEnc() != null) {
                                String decryptedName = decryptionService.decrypt(
                                        row.getCustomerCode(),
                                        row.getDekEnc(),
                                        row.getCustomerNameEnc()
                                );
                                customerName = MaskingUtils.maskName(decryptedName);
                            }

                            return OperationBoardResponse.builder()
                                    .reservationCode(row.getReservationCode())
                                    .customerCode(row.getCustomerCode())
                                    .customerName(customerName)
                                    .propertyName(row.getPropertyName())
                                    .roomType(row.getRoomType())
                                    .plannedCheckinDate(row.getPlannedCheckinDate())
                                    .plannedCheckoutDate(row.getPlannedCheckoutDate())
                                    .operationStatus(row.getOperationStatus())
                                    .build();
                        })
                        .toList();

        long total = mapper.countOperationBoard(search);

        return new PageResponse<>(
                list,
                page.getPage(),
                page.getSize(),
                total
        );
    }

    private void applySummaryFilter(OperationBoardSearchRequest search) {
        LocalDate today = LocalDate.now();

        switch (search.getSummaryType()) {
            case ALL_TODAY -> {
                search.setFromDate(today);
                search.setToDate(today);
            }
            case TODAY_CHECKIN -> {
                search.setCheckinDate(today);
                search.setReservationStatus(ReservationStatus.RESERVED);
            }
            case TODAY_CHECKOUT -> {
                search.setCheckoutDate(today);
                search.setReservationStatus(ReservationStatus.RESERVED);
            }
            case STAYING -> {
                search.setStayStatus(StayStatus.STAYING);
            }
        }
    }
}
