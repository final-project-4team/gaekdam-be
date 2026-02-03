package com.gaekdam.gaekdambe.reservation_service.timeline.query.service;

import com.gaekdam.gaekdambe.global.crypto.*;
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
    private final SearchHashService searchHashService;

    public List<TimelineCustomerResponse> findTimelineCustomers(
            Long hotelGroupCode,
            String keyword
    ) {

        String customerCodeKeyword = null;
        String nameHashHex = null;
        String phoneHashHex = null;

        if (keyword != null && !keyword.isBlank()) {

            if (keyword.matches("\\d+")) {

                // 전화번호로 판단 (보통 10~11자리)
                if (keyword.length() >= 10) {
                    String normalizedPhone = Normalizer.phone(keyword);
                    phoneHashHex = HexUtils.toHex(
                            searchHashService.phoneHash(normalizedPhone)
                    );
                }
                // 아니면 고객코드
                else {
                    customerCodeKeyword = keyword;
                }

            } else {
                // 이름
                String normalizedName = Normalizer.name(keyword);
                nameHashHex = HexUtils.toHex(
                        searchHashService.nameHash(normalizedName)
                );
            }
        }

        return mapper.findTimelineCustomers(
                        hotelGroupCode,
                        customerCodeKeyword,
                        nameHashHex,
                        phoneHashHex
                )
                .stream()
                .map(row -> {

                    String name = "(알 수 없음)";
                    String phone = "-";

                    if (row.getCustomerNameEnc() != null && row.getDekEnc() != null) {
                        String decryptedName = decryptionService.decrypt(
                                row.getCustomerCode(),
                                row.getDekEnc(),
                                row.getCustomerNameEnc()
                        );
                        name = MaskingUtils.maskName(decryptedName);
                    }

                    if (row.getPhoneEnc() != null && row.getDekEnc() != null) {
                        String decryptedPhone = decryptionService.decrypt(
                                row.getCustomerCode(),
                                row.getDekEnc(),
                                row.getPhoneEnc()
                        );
                        phone = MaskingUtils.maskPhone(decryptedPhone);
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