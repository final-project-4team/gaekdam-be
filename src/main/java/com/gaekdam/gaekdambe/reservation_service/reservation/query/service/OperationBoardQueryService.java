package com.gaekdam.gaekdambe.reservation_service.reservation.query.service;

import com.gaekdam.gaekdambe.global.crypto.DecryptionService;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.request.OperationBoardSearchRequest;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.OperationBoardCryptoRow;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.OperationBoardResponse;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.mapper.OperationBoardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OperationBoardQueryService {

    private final OperationBoardMapper mapper;
    private final DecryptionService decryptionService;

    public PageResponse<OperationBoardResponse> findOperationBoard(
            PageRequest page,
            OperationBoardSearchRequest search,
            SortRequest sort
    ) {

        List<OperationBoardResponse> list =
                mapper.findOperationBoard(page, search, sort)
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
                                    .customerName(customerName)
                                    .roomType(row.getRoomType())
                                    .propertyName(row.getPropertyName())
                                    .plannedCheckinDate(row.getPlannedCheckinDate())
                                    .plannedCheckoutDate(row.getPlannedCheckoutDate())
                                    .operationStatus(row.getOperationStatus())
                                    .build();
                        })
                        .toList();

        long total =
                mapper.countOperationBoard(search);

        return new PageResponse<>(
                list,
                page.getPage(),
                page.getSize(),
                total
        );
    }
}