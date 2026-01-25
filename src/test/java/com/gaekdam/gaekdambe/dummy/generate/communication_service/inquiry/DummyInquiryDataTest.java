package com.gaekdam.gaekdambe.dummy.generate.communication_service.inquiry;

import com.gaekdam.gaekdambe.communication_service.inquiry.command.domain.entity.Inquiry;
import com.gaekdam.gaekdambe.communication_service.inquiry.command.domain.entity.InquiryCategory;
import com.gaekdam.gaekdambe.communication_service.inquiry.command.infrastructure.repository.InquiryCategoryRepository;
import com.gaekdam.gaekdambe.communication_service.inquiry.command.infrastructure.repository.InquiryRepository;
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
public class DummyInquiryDataTest {

    private static final int TOTAL_INQUIRIES = 60_000;
    private static final int BATCH = 500;

    private static final LocalDateTime START = LocalDateTime.of(2024, 1, 1, 0, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, 12, 31, 23, 59);

    @Autowired InquiryRepository inquiryRepository;
    @Autowired InquiryCategoryRepository inquiryCategoryRepository;

    @Autowired EmployeeRepository employeeRepository;

    @PersistenceContext
    EntityManager em;

    @Transactional
    public void generate() {

        if (inquiryRepository.count() > 0) return;

        List<InquiryCategory> categories = inquiryCategoryRepository.findAll();
        if (categories.isEmpty()) return;

        // customer_code 연속 가정 제거: 실제 고객 id 로드
        List<Long> customerIds = loadIds("select customer_code from customer");
        if (customerIds.isEmpty()) return;

        // employee_code도 실제 직원 id에서 랜덤 선택
        List<Long> employeeIds = loadIds("select employee_code from employee");
        if (employeeIds.isEmpty() && employeeRepository.count() == 0) return;

        // property_code도 실제 목록에서 랜덤 선택 (없으면 1L fallback)
        List<Long> propertyIds = loadIds("select property_code from property");
        if (propertyIds.isEmpty()) propertyIds = List.of(1L);

        Random random = new Random();
        List<Inquiry> buffer = new ArrayList<>(BATCH);

        for (int i = 1; i <= TOTAL_INQUIRIES; i++) {

            long customerCode = customerIds.get(random.nextInt(customerIds.size()));
            long propertyCode = propertyIds.get(random.nextInt(propertyIds.size()));

            InquiryCategory category = categories.get(random.nextInt(categories.size()));
            LocalDateTime createdAt = randomDateTimeBetween(START, END, random);

            Inquiry inquiry = Inquiry.create(
                    propertyCode,
                    customerCode,
                    category,
                    "문의 제목 " + i,
                    "문의 내용 " + i
            );

            // 담당자 배정 55%
            if (random.nextInt(100) < 55) {
                inquiry.assignManager(pickEmployee(employeeIds, random));
            }

            // 답변 완료 45%
            if (random.nextInt(100) < 45) {
                inquiry.answer("답변 내용 " + i);
            }

            // create()가 now로 찍으니 더미 기간 분포 맞추기 위해 강제 세팅
            setCreatedAt(inquiry, createdAt);
            setUpdatedAt(inquiry, createdAt.plusHours(random.nextInt(72)));

            buffer.add(inquiry);

            if (buffer.size() == BATCH) {
                inquiryRepository.saveAll(buffer);
                em.flush();
                em.clear();
                buffer.clear();
            }
        }

        if (!buffer.isEmpty()) {
            inquiryRepository.saveAll(buffer);
            em.flush();
            em.clear();
        }
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

    private static void setCreatedAt(Inquiry inquiry, LocalDateTime createdAt) {
        try {
            var f = Inquiry.class.getDeclaredField("createdAt");
            f.setAccessible(true);
            f.set(inquiry, createdAt);
        } catch (Exception e) {
            throw new RuntimeException("Inquiry.createdAt set failed", e);
        }
    }

    private static void setUpdatedAt(Inquiry inquiry, LocalDateTime updatedAt) {
        try {
            var f = Inquiry.class.getDeclaredField("updatedAt");
            f.setAccessible(true);
            f.set(inquiry, updatedAt);
        } catch (Exception e) {
            throw new RuntimeException("Inquiry.updatedAt set failed", e);
        }
    }
}
