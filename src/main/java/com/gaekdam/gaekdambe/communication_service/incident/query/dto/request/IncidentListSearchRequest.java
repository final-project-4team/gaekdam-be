package com.gaekdam.gaekdambe.communication_service.incident.query.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IncidentListSearchRequest {

    private Long hotelGroupCode;

    private Long propertyCode;
    private String status;
    private String severity;
    private String type;

    private String fromDate;
    private String toDate;

    private String searchType;
    private String keyword;
    private byte[] employeeNameHash;

}
