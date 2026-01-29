package com.gaekdam.gaekdambe.iam_service.log.query.dto.request;


import java.time.LocalDateTime;

public record PersonalInformationLogSearchRequest(
                Long hotelGroupCode,
                String accessorLoginId,
                String permissionTypeKey,
                String purpose,
                Long personalInformationLogCode,
                String employeeAccessorName,
                String targetType,
                Long targetCode,
                String targetName,
                LocalDateTime fromDate,
                LocalDateTime toDate) {
}
