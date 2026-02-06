package com.gaekdam.gaekdambe.operation_service.facility.query.service;

import com.gaekdam.gaekdambe.global.crypto.DecryptionService;
import com.gaekdam.gaekdambe.global.crypto.MaskingUtils;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.operation_service.facility.query.dto.request.FacilityUsageSearchRequest;
import com.gaekdam.gaekdambe.operation_service.facility.query.dto.response.FacilityUsageCryptoRow;
import com.gaekdam.gaekdambe.operation_service.facility.query.dto.response.FacilityUsageResponse;
import com.gaekdam.gaekdambe.operation_service.facility.query.mapper.FacilityUsageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FacilityUsageQueryService {

    private final FacilityUsageMapper facilityUsageMapper;
    private final DecryptionService decryptionService;

    public PageResponse<FacilityUsageResponse> getFacilityUsages(
            PageRequest page,
            FacilityUsageSearchRequest search,
            SortRequest sort
    ) {
        // 오늘 범위 강제 세팅
        LocalDate today = LocalDate.now();
        search.setStartAt(today.atStartOfDay());
        search.setEndAt(today.plusDays(1).atStartOfDay());

        List<FacilityUsageCryptoRow> rows =
                facilityUsageMapper.findFacilityUsages(page, search, sort);

        long total =
                facilityUsageMapper.countFacilityUsages(search);

        List<FacilityUsageResponse> list = rows.stream()
                .map(this::toResponse)
                .toList();

        return new PageResponse<>(
                list,
                page.getPage(),
                page.getSize(),
                total
        );
    }

    private FacilityUsageResponse toResponse(FacilityUsageCryptoRow row) {
        String customerName = null;

        // 고객이 null일 수 있는 케이스(조인 누락/데이터 이상) 방어
        if (row.getCustomerCode() != null && row.getDekEnc() != null && row.getCustomerNameEnc() != null) {
            String decryptedName = decryptionService.decrypt(
                    row.getCustomerCode(),
                    row.getDekEnc(),
                    row.getCustomerNameEnc()
            );

            customerName = MaskingUtils.maskName(decryptedName);
        }

        return new FacilityUsageResponse(
                row.getFacilityUsageCode(),
                row.getUsageAt(),
                row.getUsageType(),
                row.getUsedPersonCount(),
                row.getUsageQuantity(),
                row.getUsagePrice(),
                row.getPriceSource(),
                row.getStayCode(),
                row.getCustomerCode(),
                customerName,
                row.getRoomNumber(),
                row.getFacilityCode(),
                row.getFacilityName(),
                row.getFacilityType()
        );
    }
}
