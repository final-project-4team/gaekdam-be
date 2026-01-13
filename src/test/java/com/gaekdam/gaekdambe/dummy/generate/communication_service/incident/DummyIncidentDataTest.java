package com.gaekdam.gaekdambe.dummy.generate.communication_service.incident;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Random;

import com.gaekdam.gaekdambe.communication_service.incident.command.domain.IncidentSeverity;
import com.gaekdam.gaekdambe.communication_service.incident.command.domain.IncidentType;
import com.gaekdam.gaekdambe.communication_service.incident.command.domain.entity.Incident;
import com.gaekdam.gaekdambe.communication_service.incident.command.domain.infrastructure.repository.IncidentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.transaction.Transactional;

/**
 * Incident 더미데이터 생성기
 * - 테이블이 없으면 CREATE TABLE 실행
 * - 100건의 더미 Incident 레코드 삽입
 */
@Component
@Transactional
public class DummyIncidentDataTest {

    @Autowired
    private IncidentRepository incidentRepository;

    public void generate() {

        // 이미 데이터 있으면 스킵
        if (incidentRepository.count() > 0) {
            return;
        }

        Random rnd = new Random(20260110);

        IncidentType[] types = IncidentType.values();
        IncidentSeverity[] severities = IncidentSeverity.values();

        int total = 100;

        for (int i = 1; i <= total; i++) {

            IncidentType type = types[rnd.nextInt(types.length)];
            IncidentSeverity severity = severities[rnd.nextInt(severities.length)];

            LocalDateTime occuredAt =
                    LocalDateTime.now()
                            .minusDays(rnd.nextInt(60))
                            .minusHours(rnd.nextInt(24));

            Incident incident = Incident.create(
                    1L + rnd.nextInt(5),          // hotelGroupCode
                    1L + rnd.nextInt(20),         // propertyCode
                    100L + rnd.nextInt(50),       // employeeCode
                    String.format("사건 보고 #%03d - %s", i, type.name()),
                    String.format("간단 요약입니다. 사건번호=%03d", i),
                    String.format(
                            "상세 보고 내용 (샘플) - 사건번호=%03d. 발생상세: 자세한 내용 작성 필요.",
                            i
                    ),
                    type,
                    severity,
                    occuredAt,
                    null
            );

            // 일부는 close
            if (rnd.nextInt(10) < 3) {
                incident.close();
            }

            incidentRepository.save(incident);
        }
    }
}
