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

    private static final int TOTAL_INQUIRIES = 150_000;
    private static final int BATCH = 500;

    private static final LocalDateTime START = LocalDateTime.of(2024, 1, 1, 0, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, 12, 31, 23, 59);

    @Autowired
    InquiryRepository inquiryRepository;
    @Autowired
    InquiryCategoryRepository inquiryCategoryRepository;

    @Autowired
    EmployeeRepository employeeRepository;

    @PersistenceContext
    EntityManager em;

    @Transactional
    public void generate() {

        // 기존 데이터가 있으면 삭제하고 다시 생성 (데이터 분포 재조정을 위해)
        if (inquiryRepository.count() > 0) {
            // FK 제약조건 때문에 자식 테이블 데이터 먼저 삭제 필요
            em.createNativeQuery("DELETE FROM incident_action_history").executeUpdate();
            em.createNativeQuery("DELETE FROM incident").executeUpdate();
            // message_send_history 등은 inquiry와 직접 연관 없거나 이미 삭제되었다고 가정

            inquiryRepository.deleteAll();
            em.flush();
            em.clear();
        }

        List<InquiryCategory> categories = inquiryCategoryRepository.findAll();
        if (categories.isEmpty())
            return;

        // customer_code, customer_status, hotel_group_code 조회
        List<CustomerInfo> customers = loadCustomerInfos();
        if (customers.isEmpty())
            return;

        // employee_code도 실제 직원 id에서 랜덤 선택
        List<Long> employeeIds = loadIds("select employee_code from employee");
        if (employeeIds.isEmpty() && employeeRepository.count() == 0)
            return;

        // 데모 시연을 위해, 첫 번째 property(아마도 1번)에 60% 몰아주기
        // 그래야 해당 지점으로 로그인했을 때 문의 내역이 풍성하게 보임
        // 나머지 40%는 다른 지점에 랜덤 분배 (데이터 현실성)
        List<Long> propertyIds = loadIds("select property_code from property order by property_code asc");
        if (propertyIds.isEmpty()) {
            propertyIds = List.of(1L);
        }

        Random random = new Random();
        List<Inquiry> buffer = new ArrayList<>(BATCH);

        // 1. 전체 고객 셔플 (순서 섞기)
        Collections.shuffle(customers);

        // 2. 모든 고객에게 최소 1개씩 문의 생성
        int totalCreated = 0;
        for (CustomerInfo customer : customers) {
            createInquiry(customer, categories, propertyIds, employeeIds, random, buffer, totalCreated++);
        }

        // 3. 남은 개수는 호텔 그룹 1~3번 고객에게 가중치 부여하여 생성
        // (타겟 고객 리스트 별도 추출)
        List<CustomerInfo> targetCustomers = customers.stream()
                .filter(c -> c.hotelGroupCode != null && c.hotelGroupCode <= 3L)
                .toList();

        // 만약 1~3번 고객이 너무 적으면 전체에서 랜덤
        if (targetCustomers.isEmpty()) {
            targetCustomers = customers;
        }

        while (totalCreated < TOTAL_INQUIRIES) {
            CustomerInfo target = targetCustomers.get(random.nextInt(targetCustomers.size()));
            createInquiry(target, categories, propertyIds, employeeIds, random, buffer, totalCreated++);
        }

        if (!buffer.isEmpty()) {
            inquiryRepository.saveAll(buffer);
            em.flush();
            em.clear();
        }
    }

    private void createInquiry(CustomerInfo customer, List<InquiryCategory> categories, List<Long> propertyIds,
                               List<Long> employeeIds, Random random, List<Inquiry> buffer, int i) {

        long propertyCode;
        if (propertyIds.size() > 1) {
            // 60% 확률로 첫 번째 지점 (데모용)
            if (random.nextInt(100) < 60) {
                propertyCode = propertyIds.get(0);
            } else {
                // 나머지 40%는 전체에서 랜덤 (첫 번째 포함)
                propertyCode = propertyIds.get(random.nextInt(propertyIds.size()));
            }
        } else {
            propertyCode = propertyIds.get(0);
        }

        InquiryCategory category = categories.get(random.nextInt(categories.size()));
        LocalDateTime createdAt = randomDateTimeBetween(START, END, random);

        Inquiry inquiry = Inquiry.create(
                propertyCode,
                customer.customerCode,
                category,
                "문의 제목 " + i,
                "문의 내용 " + i);

        // 담당자 배정 55%
        if (random.nextInt(100) < 55) {
            inquiry.assignManager(pickEmployee(employeeIds, random));
        }

        // 일반(ACTIVE): 45% 답변 완료
        // 주의(CAUTION): 10% 답변 완료 (90% 미해결)
        int answerProbability = 45;
        if ("CAUTION".equals(customer.customerStatus)) {
            answerProbability = 10;
        }

        if (random.nextInt(100) < answerProbability) {
            inquiry.answer("답변 내용 " + i);
        }

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

    private Long pickEmployee(List<Long> employeeIds, Random random) {
        if (employeeIds == null || employeeIds.isEmpty())
            return 1L;
        return employeeIds.get(random.nextInt(employeeIds.size()));
    }

    private record CustomerInfo(Long customerCode, String customerStatus, Long hotelGroupCode) {
    }

    private List<CustomerInfo> loadCustomerInfos() {
        String sql = "select customer_code, customer_status, hotel_group_code from customer";
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery(sql).getResultList();

        List<CustomerInfo> infos = new ArrayList<>(rows.size());
        for (Object[] row : rows) {
            Long code = ((Number) row[0]).longValue();
            String status = (String) row[1];
            Long hgCode = row[2] != null ? ((Number) row[2]).longValue() : null;
            infos.add(new CustomerInfo(code, status, hgCode));
        }
        return infos;
    }

    private List<Long> loadIds(String sql) {
        @SuppressWarnings("unchecked")
        List<Object> rows = em.createNativeQuery(sql).getResultList();

        List<Long> ids = new ArrayList<>(rows.size());
        for (Object o : rows) {
            if (o instanceof Number n)
                ids.add(n.longValue());
            else if (o instanceof String s)
                ids.add(Long.parseLong(s));
        }
        return ids;
    }

    private static LocalDateTime randomDateTimeBetween(LocalDateTime start, LocalDateTime end, Random random) {
        long seconds = Duration.between(start, end).getSeconds();
        if (seconds <= 0)
            return start;
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
