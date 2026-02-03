package com.gaekdam.gaekdambe.dummy.generate.communication_service.incident;

import com.gaekdam.gaekdambe.communication_service.incident.command.domain.IncidentSeverity;
import com.gaekdam.gaekdambe.communication_service.incident.command.domain.IncidentType;
import com.gaekdam.gaekdambe.communication_service.incident.command.domain.entity.Incident;
import com.gaekdam.gaekdambe.communication_service.incident.command.domain.entity.IncidentActionHistory;
import com.gaekdam.gaekdambe.communication_service.incident.command.infrastructure.repository.IncidentActionHistoryRepository;
import com.gaekdam.gaekdambe.communication_service.incident.command.infrastructure.repository.IncidentRepository;
import com.gaekdam.gaekdambe.communication_service.inquiry.command.domain.entity.Inquiry;
import com.gaekdam.gaekdambe.iam_service.employee.command.infrastructure.EmployeeRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class DummyIncidentDataTest {

    private static final int TOTAL_INCIDENTS = 30_000;
    private static final int BATCH = 500;

    private static final LocalDateTime START = LocalDateTime.of(2024, 1, 1, 0, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, 12, 31, 23, 59);

    @Autowired IncidentRepository incidentRepository;
    @Autowired IncidentActionHistoryRepository actionHistoryRepository;

    @Autowired EmployeeRepository employeeRepository;

    @PersistenceContext
    EntityManager em;

    @Transactional
    public void generate() {

        if (incidentRepository.count() > 0) return;

        Random random = new Random();

        List<Long> inquiryIds = loadIds("select inquiry_code from inquiry");

        List<Long> employeeIds = loadIds("select employee_code from employee");
        if (employeeIds.isEmpty() && employeeRepository.count() == 0) return;

        List<Long> propertyIds = loadIds("select property_code from property");
        if (propertyIds.isEmpty()) propertyIds = List.of(1L);

        List<Incident> incidentBuffer = new ArrayList<>(BATCH);
        List<IncidentActionHistory> actionBuffer = new ArrayList<>(BATCH * 2);

        for (int i = 1; i <= TOTAL_INCIDENTS; i++) {

            LocalDateTime occurredAt = randomDateTimeBetween(START, END, random);

            IncidentType type = IncidentType.values()[random.nextInt(IncidentType.values().length)];
            IncidentSeverity severity = IncidentSeverity.values()[random.nextInt(IncidentSeverity.values().length)];

            Long propertyCode = propertyIds.get(random.nextInt(propertyIds.size()));
            Long employeeCode = pickEmployee(employeeIds, random);

            Inquiry inquiryRef = null;

            // 문의 연결 비율 50%
            if (!inquiryIds.isEmpty() && random.nextInt(100) < 50) {
                Long inquiryCode = inquiryIds.get(random.nextInt(inquiryIds.size()));
                inquiryRef = em.getReference(Inquiry.class, inquiryCode);
            }

            Incident incident = Incident.create(
                    propertyCode,
                    employeeCode,
                    "사건/사고 제목 " + i,
                    "사건 요약 " + i,
                    "사건 상세 내용 " + i,
                    type,
                    severity,
                    occurredAt,
                    inquiryRef
            );

            // createdAt/updatedAt 기간 분포 맞춤
            setCreatedAt(incident, occurredAt);

            // 50% 종결 상태 만들기
            if (random.nextInt(100) < 50) {
                incident.close(); // incidentStatus=CLOSED + updatedAt=now 찍힘
            }

            // close()가 updatedAt=now 찍은 값 덮어서 기간 분포 맞춤
            setUpdatedAt(incident, occurredAt.plusHours(random.nextInt(72)));

            incidentBuffer.add(incident);

            if (incidentBuffer.size() == BATCH) {
                flushBatch(incidentBuffer, actionBuffer, employeeIds, random);
            }
        }

        if (!incidentBuffer.isEmpty()) {
            flushBatch(incidentBuffer, actionBuffer, employeeIds, random);
        }
    }

    private void flushBatch(
            List<Incident> incidentBuffer,
            List<IncidentActionHistory> actionBuffer,
            List<Long> employeeIds,
            Random random
    ) {
        incidentRepository.saveAll(incidentBuffer);
        em.flush();

        for (Incident saved : incidentBuffer) {

            // 조치 이력 생성 비율 50%면 1~2건, 아니면 0건
            int actionCount = (random.nextInt(100) < 50) ? (random.nextInt(2) + 1) : 0;

            for (int a = 0; a < actionCount; a++) {
                Long employeeCode = pickEmployee(employeeIds, random);
                actionBuffer.add(
                        IncidentActionHistory.create(
                                saved,
                                employeeCode,
                                "조치 내용 " + saved.getIncidentCode() + "-" + (a + 1)
                        )
                );
            }
        }

        if (!actionBuffer.isEmpty()) {
            actionHistoryRepository.saveAll(actionBuffer);
        }

        em.flush();
        em.clear();

        incidentBuffer.clear();
        actionBuffer.clear();
    }

    private Long pickEmployee(List<Long> employeeIds, Random random) {
        if (employeeIds == null || employeeIds.isEmpty()) return 1L;
        return employeeIds.get(random.nextInt(employeeIds.size()));
    }

    private List<Long> loadIds(String sql) {
        @SuppressWarnings("unchecked")
        List<Object> rows = em.createNativeQuery(sql).getResultList();

        List<Long> ids = new ArrayList<>(rows.size());
        for (Object o : rows) {
            if (o instanceof Number n) ids.add(n.longValue());
            else if (o instanceof String s) ids.add(Long.parseLong(s));
        }
        return ids;
    }

    private static LocalDateTime randomDateTimeBetween(LocalDateTime start, LocalDateTime end, Random random) {
        long seconds = Duration.between(start, end).getSeconds();
        if (seconds <= 0) return start;
        long add = (random.nextLong() & Long.MAX_VALUE) % seconds;
        return start.plusSeconds(add);
    }

    private static void setCreatedAt(Incident incident, LocalDateTime createdAt) {
        try {
            var f = Incident.class.getDeclaredField("createdAt");
            f.setAccessible(true);
            f.set(incident, createdAt);
        } catch (Exception e) {
            throw new RuntimeException("Incident.createdAt set failed", e);
        }
    }

    private static void setUpdatedAt(Incident incident, LocalDateTime updatedAt) {
        try {
            var f = Incident.class.getDeclaredField("updatedAt");
            f.setAccessible(true);
            f.set(incident, updatedAt);
        } catch (Exception e) {
            throw new RuntimeException("Incident.updatedAt set failed", e);
        }
    }
}
