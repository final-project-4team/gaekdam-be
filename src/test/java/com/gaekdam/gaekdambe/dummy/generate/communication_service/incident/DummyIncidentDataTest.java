package com.gaekdam.gaekdambe.dummy.generate.communication_service.incident;

import com.gaekdam.gaekdambe.communication_service.incident.command.domain.IncidentSeverity;
import com.gaekdam.gaekdambe.communication_service.incident.command.domain.IncidentType;
import com.gaekdam.gaekdambe.communication_service.incident.command.domain.entity.Incident;
import com.gaekdam.gaekdambe.communication_service.incident.command.domain.entity.IncidentActionHistory;
import com.gaekdam.gaekdambe.communication_service.incident.command.infrastructure.repository.IncidentActionHistoryRepository;
import com.gaekdam.gaekdambe.communication_service.incident.command.infrastructure.repository.IncidentRepository;
import com.gaekdam.gaekdambe.communication_service.inquiry.command.domain.entity.Inquiry;
import com.gaekdam.gaekdambe.communication_service.inquiry.command.infrastructure.repository.InquiryRepository;
import com.gaekdam.gaekdambe.hotel_service.property.command.domain.entity.Property;
import com.gaekdam.gaekdambe.hotel_service.property.command.infrastructure.PropertyRepository;
import com.gaekdam.gaekdambe.iam_service.employee.command.domain.entity.Employee;
import com.gaekdam.gaekdambe.iam_service.employee.command.infrastructure.EmployeeRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Component
@Transactional
public class DummyIncidentDataTest {

    private final IncidentRepository incidentRepository;
    private final IncidentActionHistoryRepository incidentActionHistoryRepository;
    private final PropertyRepository propertyRepository;
    private final EmployeeRepository employeeRepository;
    private final InquiryRepository inquiryRepository;

    private static final int TOTAL = 100;
    private static final long SEED = 20260110L;

    public DummyIncidentDataTest(
            IncidentRepository incidentRepository,
            IncidentActionHistoryRepository incidentActionHistoryRepository,
            PropertyRepository propertyRepository,
            EmployeeRepository employeeRepository,
            InquiryRepository inquiryRepository
    ) {
        this.incidentRepository = incidentRepository;
        this.incidentActionHistoryRepository = incidentActionHistoryRepository;
        this.propertyRepository = propertyRepository;
        this.employeeRepository = employeeRepository;
        this.inquiryRepository = inquiryRepository;
    }

    public void generate() {

        // 이미 데이터 있으면 스킵
        if (incidentRepository.count() > 0) return;

        //  실제 PK 목록을 DB에서 가져와서 사용
        List<Long> propertyCodes = propertyRepository.findAll().stream()
                .map(Property::getPropertyCode)
                .toList();

        List<Long> employeeCodes = employeeRepository.findAll().stream()
                .map(Employee::getEmployeeCode)
                .toList();

        List<Inquiry> inquiries = inquiryRepository.findAll(); // 일부만 연결 (없으면 null)

        if (propertyCodes.isEmpty()) throw new IllegalStateException("property 데이터가 없어서 incident 더미 생성 불가");
        if (employeeCodes.isEmpty()) throw new IllegalStateException("employee 데이터가 없어서 incident 더미 생성 불가");

        Random rnd = new Random(SEED);

        IncidentType[] types = IncidentType.values();
        IncidentSeverity[] severities = IncidentSeverity.values();

        for (int i = 1; i <= TOTAL; i++) {

            Long propertyCode = propertyCodes.get(rnd.nextInt(propertyCodes.size()));
            Long employeeCode = employeeCodes.get(rnd.nextInt(employeeCodes.size()));

            IncidentType type = types[rnd.nextInt(types.length)];
            IncidentSeverity severity = severities[rnd.nextInt(severities.length)];

            LocalDateTime occurredAt = LocalDateTime.now()
                    .minusDays(rnd.nextInt(60))
                    .minusHours(rnd.nextInt(24));

            //  20%만 inquiry 연결 (그리고 "실제로 존재하는 inquiry"만)
            Inquiry inquiry = null;
            if (!inquiries.isEmpty() && rnd.nextInt(10) < 2) {
                inquiry = inquiries.get(rnd.nextInt(inquiries.size()));
            }

            Incident incident = Incident.create(
                    propertyCode,
                    employeeCode,
                    String.format("사건 보고 #%03d - %s", i, type.name()),
                    String.format("간단 요약입니다. 사건번호=%03d", i),
                    String.format("상세 보고 내용 (샘플) - 사건번호=%03d.", i),
                    type,
                    severity,
                    occurredAt,
                    inquiry
            );

            // 일부는 close
            if (rnd.nextInt(10) < 3) incident.close();

            Incident saved = incidentRepository.save(incident);

            //  ActionHistory 1~3개 생성
            int historyCount = 1 + rnd.nextInt(3);
            for (int h = 1; h <= historyCount; h++) {
                Long actionEmp = employeeCodes.get(rnd.nextInt(employeeCodes.size()));
                incidentActionHistoryRepository.save(
                        IncidentActionHistory.create(
                                saved,
                                actionEmp,
                                String.format("조치 이력 #%d (incident=%d)", h, saved.getIncidentCode())
                        )
                );
            }
        }
    }
}
