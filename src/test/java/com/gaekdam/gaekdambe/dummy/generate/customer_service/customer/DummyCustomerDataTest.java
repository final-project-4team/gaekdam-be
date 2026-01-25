package com.gaekdam.gaekdambe.dummy.generate.customer_service.customer;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.ChangeSource;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.ContactType;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.ContractType;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.CustomerStatus;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.NationalityType;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.entity.Customer;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.entity.CustomerContact;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.entity.CustomerMemo;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.entity.CustomerStatusHistory;
import com.gaekdam.gaekdambe.customer_service.customer.command.infrastructure.repository.CustomerContactRepository;
import com.gaekdam.gaekdambe.customer_service.customer.command.infrastructure.repository.CustomerMemoRepository;
import com.gaekdam.gaekdambe.customer_service.customer.command.infrastructure.repository.CustomerRepository;
import com.gaekdam.gaekdambe.customer_service.customer.command.infrastructure.repository.CustomerStatusHistoryRepository;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.domain.entity.HotelGroup;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.infrastructure.repository.HotelGroupRepository;
import com.gaekdam.gaekdambe.iam_service.employee.command.domain.EmployeeStatus;
import com.gaekdam.gaekdambe.iam_service.employee.command.domain.entity.Employee;
import com.gaekdam.gaekdambe.iam_service.employee.command.infrastructure.EmployeeRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class DummyCustomerDataTest {

    private static final int TOTAL_CUSTOMERS = 50_000;
    private static final int BATCH = 500;

    private static final String KMS_KEY_ID = "dummy-kms-key";

    private static final LocalDateTime START = LocalDateTime.of(2024, 1, 1, 0, 0);
    private static final LocalDateTime END   = LocalDateTime.of(2026, 12, 31, 23, 59);

    @Autowired CustomerRepository customerRepository;
    @Autowired CustomerContactRepository contactRepository;
    @Autowired CustomerMemoRepository memoRepository;
    @Autowired CustomerStatusHistoryRepository statusHistoryRepository;

    @Autowired HotelGroupRepository hotelGroupRepository;
    @Autowired EmployeeRepository employeeRepository;

    @PersistenceContext
    EntityManager em;

    @Transactional
    public void generate() {

        if (customerRepository.count() > 0) return;

        List<HotelGroup> hotelGroups = hotelGroupRepository.findAll();
        if (hotelGroups.isEmpty()) return;

        List<Long> hotelGroupCodes = hotelGroups.stream()
                .map(HotelGroup::getHotelGroupCode)
                .filter(Objects::nonNull)
                .toList();

        if (hotelGroupCodes.isEmpty()) return;

        Map<Long, List<Long>> employeeCodesByHotelGroup = buildEmployeeCodesByHotelGroup();
        List<Long> fallbackEmployeeCodes = employeeCodesByHotelGroup.values()
                .stream()
                .flatMap(List::stream)
                .distinct()
                .toList();

        if (fallbackEmployeeCodes.isEmpty()) return;

        Random random = new Random();

        List<Customer> customerBuffer = new ArrayList<>(BATCH);
        List<Integer> indexBuffer = new ArrayList<>(BATCH);
        List<Long> hotelGroupBuffer = new ArrayList<>(BATCH);

        for (int i = 1; i <= TOTAL_CUSTOMERS; i++) {

            LocalDateTime createdAt = randomDateTimeBetween(START, END, random);

            NationalityType nationality =
                    (random.nextInt(100) < 80) ? NationalityType.DOMESTIC : NationalityType.FOREIGN;

            ContractType contractType =
                    (random.nextInt(100) < 85) ? ContractType.INDIVIDUAL : ContractType.CORPORATE;

            long hotelGroupCode = hotelGroupCodes.get((i - 1) % hotelGroupCodes.size());

            String name = makeName(i, nationality, contractType);
            byte[] nameEnc = name.getBytes(StandardCharsets.UTF_8);
            String nameHash = sha256Hex(name);

            byte[] dekEnc = randomBytes(random, 64);

            Customer customer = Customer.createCustomer(
                    hotelGroupCode,
                    nameEnc,
                    nameHash,
                    nationality,
                    contractType,
                    KMS_KEY_ID,
                    dekEnc,
                    createdAt
            );

            customerBuffer.add(customer);
            indexBuffer.add(i);
            hotelGroupBuffer.add(hotelGroupCode);

            if (customerBuffer.size() == BATCH) {
                flushCustomerBatch(customerBuffer, indexBuffer, hotelGroupBuffer, employeeCodesByHotelGroup, fallbackEmployeeCodes, random);
            }
        }

        if (!customerBuffer.isEmpty()) {
            flushCustomerBatch(customerBuffer, indexBuffer, hotelGroupBuffer, employeeCodesByHotelGroup, fallbackEmployeeCodes, random);
        }
    }

    private Map<Long, List<Long>> buildEmployeeCodesByHotelGroup() {
        List<Employee> employees = employeeRepository.findAll();

        Map<Long, List<Long>> map = new HashMap<>();
        for (Employee e : employees) {
            if (e == null) continue;
            if (e.getEmployeeCode() == null) continue;
            if (e.getEmployeeStatus() != EmployeeStatus.ACTIVE) continue;
            if (e.getHotelGroup() == null || e.getHotelGroup().getHotelGroupCode() == null) continue;

            Long hotelGroupCode = e.getHotelGroup().getHotelGroupCode();
            map.computeIfAbsent(hotelGroupCode, k -> new ArrayList<>()).add(e.getEmployeeCode());
        }

        map.values().forEach(list -> list.sort(Comparator.naturalOrder()));
        return map;
    }

    private void flushCustomerBatch(
            List<Customer> customerBuffer,
            List<Integer> indexBuffer,
            List<Long> hotelGroupBuffer,
            Map<Long, List<Long>> employeeCodesByHotelGroup,
            List<Long> fallbackEmployeeCodes,
            Random random
    ) {
        customerRepository.saveAll(customerBuffer);
        em.flush();

        List<CustomerContact> contactBuffer = new ArrayList<>(customerBuffer.size() * 2);
        List<CustomerMemo> memoBuffer = new ArrayList<>(customerBuffer.size());
        List<CustomerStatusHistory> statusBuffer = new ArrayList<>(customerBuffer.size());

        for (int i = 0; i < customerBuffer.size(); i++) {
            Customer customer = customerBuffer.get(i);
            int idx = indexBuffer.get(i);
            Long hotelGroupCode = hotelGroupBuffer.get(i);

            Long customerCode = customer.getCustomerCode();
            LocalDateTime createdAt = customer.getCreatedAt() != null ? customer.getCreatedAt() : LocalDateTime.now();

            Long employeeCode = pickEmployeeCode(hotelGroupCode, employeeCodesByHotelGroup, fallbackEmployeeCodes, random);

            String phone = makePhone(idx);
            contactBuffer.add(
                    CustomerContact.createCustomerContact(
                            customerCode,
                            ContactType.PHONE,
                            phone.getBytes(StandardCharsets.UTF_8),
                            sha256Hex(phone),
                            true,
                            random.nextInt(100) < 35,
                            createdAt,
                            createdAt
                    )
            );

            if (random.nextInt(100) < 65) {
                String email = "user" + idx + "@example.com";
                contactBuffer.add(
                        CustomerContact.createCustomerContact(
                                customerCode,
                                ContactType.EMAIL,
                                email.getBytes(StandardCharsets.UTF_8),
                                sha256Hex(email),
                                false,
                                random.nextInt(100) < 25,
                                createdAt,
                                createdAt
                        )
                );
            }

            if (random.nextInt(100) < 20) {
                memoBuffer.add(
                        CustomerMemo.registerCustomerMemo(
                                customerCode,
                                employeeCode,
                                "dummy memo for customer " + idx,
                                createdAt
                        )
                );
            }

            // history는 후처리에서 상태를 만들 거라, 과하게 넣지 않음
            if (random.nextInt(100) < 3) {
                LocalDateTime changedAt = createdAt.plusDays(1 + random.nextInt(30));
                if (changedAt.isAfter(END)) changedAt = END;

                statusBuffer.add(
                        CustomerStatusHistory.recordCustomerStatusChange(
                                customerCode,
                                CustomerStatus.ACTIVE,
                                CustomerStatus.CAUTION,
                                ChangeSource.SYSTEM,
                                employeeCode,
                                "dummy status history",
                                changedAt
                        )
                );
            }
        }

        if (!contactBuffer.isEmpty()) contactRepository.saveAll(contactBuffer);
        if (!memoBuffer.isEmpty()) memoRepository.saveAll(memoBuffer);
        if (!statusBuffer.isEmpty()) statusHistoryRepository.saveAll(statusBuffer);

        em.flush();
        em.clear();

        customerBuffer.clear();
        indexBuffer.clear();
        hotelGroupBuffer.clear();
    }

    private Long pickEmployeeCode(
            Long hotelGroupCode,
            Map<Long, List<Long>> employeeCodesByHotelGroup,
            List<Long> fallbackEmployeeCodes,
            Random random
    ) {
        List<Long> list = employeeCodesByHotelGroup.get(hotelGroupCode);
        if (list != null && !list.isEmpty()) {
            return list.get(random.nextInt(list.size()));
        }
        return fallbackEmployeeCodes.get(random.nextInt(fallbackEmployeeCodes.size()));
    }

    private static String makeName(int idx, NationalityType n, ContractType c) {
        if (c == ContractType.CORPORATE) return "법인고객_" + idx;
        return (n == NationalityType.DOMESTIC) ? "개인고객_" + idx : "FOREIGN_" + idx;
    }

    private static String makePhone(int idx) {
        int mid = (idx % 9000) + 1000;
        int end = ((idx * 7) % 9000) + 1000;
        return "010" + mid + end;
    }

    private static LocalDateTime randomDateTimeBetween(LocalDateTime start, LocalDateTime end, Random random) {
        long seconds = Duration.between(start, end).getSeconds();
        if (seconds <= 0) return start;
        long add = (random.nextLong() & Long.MAX_VALUE) % seconds;
        return start.plusSeconds(add);
    }

    private static byte[] randomBytes(Random random, int len) {
        byte[] b = new byte[len];
        random.nextBytes(b);
        return b;
    }

    private static String sha256Hex(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] dig = md.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(dig.length * 2);
            for (byte x : dig) sb.append(String.format("%02x", x));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
