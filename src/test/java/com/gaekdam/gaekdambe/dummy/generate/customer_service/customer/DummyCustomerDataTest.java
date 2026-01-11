package com.gaekdam.gaekdambe.dummy.generate.customer_service.customer;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.*;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.entity.*;
import com.gaekdam.gaekdambe.customer_service.customer.command.infrastructure.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class DummyCustomerDataTest {

    @Autowired private CustomerRepository customerRepository;
    @Autowired private CustomerContactRepository customerContactRepository;
    @Autowired private CustomerMemoRepository customerMemoRepository;
    @Autowired private MemberRepository memberRepository;

    @Autowired(required = false)
    private CustomerStatusHistoryRepository customerStatusHistoryRepository;

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public void generate() {

        if (customerRepository.count() > 0) {
            return;
        }

        int count = Integer.getInteger("dummy.customer.count", 1000);
        List<Long> hotelGroupCodes = List.of(1L, 2L, 3L);
        LocalDateTime now = LocalDateTime.now();

        for (int i = 1; i <= count; i++) {
            Long hotelGroupCode = hotelGroupCodes.get((i - 1) % hotelGroupCodes.size());
            LocalDateTime createdAt = now.minusDays(randomInt(0, 365));

            createFullCustomerData(i, hotelGroupCode, createdAt);

            if (i % 100 == 0) {
                em.flush();
                em.clear();
            }
        }
    }

    private Customer createFullCustomerData(int seq, Long hotelGroupCode, LocalDateTime createdAt) {

        String customerName = "고객" + seq;
        byte[] customerNameEnc = fakeEnc(customerName);
        String customerNameHash = sha256Hex(customerName);

        NationalityType nationalityType = pickNationalityType();
        ContractType contractType = pickContractType();

        String kmsKeyId = "kms-key-dev-001";
        byte[] dekEnc = randomBytes(64);

        // ✅ customerType 제거된 createCustomer 사용
        Customer customer = Customer.createCustomer(
                hotelGroupCode,
                customerNameEnc,
                customerNameHash,
                nationalityType,
                contractType,
                kmsKeyId,
                dekEnc,
                createdAt
        );

        customerRepository.save(customer);

        CustomerStatus before = customer.getCustomerStatus();
        CustomerStatus after = pickCustomerStatus();
        if (after != before) {
            LocalDateTime changedAt = createdAt.plusDays(randomInt(1, 30));
            customer.changeCustomerStatus(after, changedAt);

            if (customerStatusHistoryRepository != null) {
                customerStatusHistoryRepository.save(
                        CustomerStatusHistory.recordCustomerStatusChange(
                                customer.getCustomerCode(),
                                before,
                                after,
                                ChangeSource.SYSTEM,
                                null,
                                "더미 상태 변경",
                                changedAt
                        )
                );
            }
        }

        createContacts(customer.getCustomerCode(), seq, createdAt);

        if (chance(0.60)) {
            memberRepository.save(Member.registerMember(customer.getCustomerCode(), createdAt));
        }

        if (chance(0.30)) {
            int memoCount = randomInt(1, 3);
            for (int m = 0; m < memoCount; m++) {
                customerMemoRepository.save(
                        CustomerMemo.registerCustomerMemo(
                                customer.getCustomerCode(),
                                1L,
                                "더미 메모 " + (m + 1) + " - " + customerName,
                                createdAt.plusMinutes(m)
                        )
                );
            }
        }

        return customer;
    }

    private void createContacts(Long customerCode, int seq, LocalDateTime createdAt) {

        String phone = "010" + String.format("%08d", seq);
        Boolean phoneOptIn = chance(0.50);
        LocalDateTime phoneConsentAt = phoneOptIn ? createdAt : null;

        customerContactRepository.save(
                CustomerContact.createCustomerContact(
                        customerCode,
                        ContactType.PHONE,
                        fakeEnc(phone),
                        sha256Hex(phone),
                        Boolean.TRUE,
                        phoneOptIn,
                        phoneConsentAt,
                        createdAt
                )
        );

        String email = "dummy" + seq + "@gaekdam.test";
        Boolean emailOptIn = chance(0.50);
        LocalDateTime emailConsentAt = emailOptIn ? createdAt : null;

        customerContactRepository.save(
                CustomerContact.createCustomerContact(
                        customerCode,
                        ContactType.EMAIL,
                        fakeEnc(email),
                        sha256Hex(email),
                        Boolean.FALSE,
                        emailOptIn,
                        emailConsentAt,
                        createdAt
                )
        );
    }

    private NationalityType pickNationalityType() {
        return chance(0.85) ? NationalityType.DOMESTIC : NationalityType.FOREIGN;
    }

    private ContractType pickContractType() {
        return chance(0.80) ? ContractType.PERSONAL : ContractType.CORPORATE;
    }

    private CustomerStatus pickCustomerStatus() {
        double r = ThreadLocalRandom.current().nextDouble();
        if (r < 0.05) return CustomerStatus.INACTIVE;
        if (r < 0.15) return CustomerStatus.CAUTION;
        return CustomerStatus.ACTIVE;
    }

    private boolean chance(double probability) {
        return ThreadLocalRandom.current().nextDouble() < probability;
    }

    private int randomInt(int minInclusive, int maxExclusive) {
        return ThreadLocalRandom.current().nextInt(minInclusive, maxExclusive);
    }

    private byte[] fakeEnc(String plain) {
        return plain.getBytes(StandardCharsets.UTF_8);
    }

    private byte[] randomBytes(int size) {
        byte[] b = new byte[size];
        ThreadLocalRandom.current().nextBytes(b);
        return b;
    }

    private String sha256Hex(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte d : digest) sb.append(String.format("%02x", d));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("sha256 failed", e);
        }
    }
}
