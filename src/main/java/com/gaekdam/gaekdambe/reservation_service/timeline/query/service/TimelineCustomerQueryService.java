package com.gaekdam.gaekdambe.reservation_service.timeline.query.service;

import com.gaekdam.gaekdambe.global.crypto.DecryptionService;
import com.gaekdam.gaekdambe.reservation_service.timeline.query.dto.response.TimelineCustomerResponse;
import com.gaekdam.gaekdambe.reservation_service.timeline.query.mapper.TimelineCustomerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TimelineCustomerQueryService {

    private final TimelineCustomerMapper mapper;
    private final DecryptionService decryptionService;

    public List<TimelineCustomerResponse> findTimelineCustomers(
            Long hotelGroupCode,
            String keyword
    ) {

        return mapper.findTimelineCustomers(hotelGroupCode, keyword)
                .stream()
                .map(row -> {

                    String name = "(알 수 없음)";
                    String phone = "-";

                    if (row.getCustomerNameEnc() != null && row.getDekEnc() != null) {
                        name = decryptionService.decrypt(
                                row.getCustomerCode(),
                                row.getDekEnc(),
                                row.getCustomerNameEnc()
                        );
                    }

                    if (row.getPhoneEnc() != null && row.getDekEnc() != null) {
                        phone = decryptionService.decrypt(
                                row.getCustomerCode(),
                                row.getDekEnc(),
                                row.getPhoneEnc()
                        );
                    }

                    return new TimelineCustomerResponse(
                            row.getCustomerCode(),
                            name,
                            phone
                    );
                })
                .toList();
    }
}
