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

    private Long hotelGroupCode;      // 컨트롤러에서 강제 주입

    private Long propertyCode;        // 선택(없으면 전체 지점)
    private String status;            // IN_PROGRESS / CLOSED
    private String severity;          // LOW/MEDIUM/HIGH/CRITICAL
    private String type;              // 시설/결재/고객/직원/기타(너 enum명으로)

    private String keyword;           // 제목/내용 검색

    private LocalDate fromDate;       // created_at 기준
    private LocalDate toDate;
}
