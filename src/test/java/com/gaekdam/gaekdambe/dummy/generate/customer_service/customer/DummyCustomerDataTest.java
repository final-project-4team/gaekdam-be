package com.gaekdam.gaekdambe.dummy.generate.customer_service.customer;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.*;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.entity.*;
import com.gaekdam.gaekdambe.customer_service.customer.command.infrastructure.repository.*;
import com.gaekdam.gaekdambe.global.crypto.AesCryptoUtils;
import com.gaekdam.gaekdambe.global.crypto.DataKey;
import com.gaekdam.gaekdambe.global.crypto.KmsService;
import com.gaekdam.gaekdambe.global.crypto.SearchHashService;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.domain.entity.HotelGroup;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.infrastructure.repository.HotelGroupRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired private HotelGroupRepository hotelGroupRepository;

    @Autowired private KmsService kmsService;
    @Autowired private SearchHashService searchHashService;

    @PersistenceContext
    private EntityManager em;


    // 기본 5000명 생성
    @Transactional
    public void generate() {
        List<Long> hotelGroupCodes = hotelGroupRepository.findAll().stream()
                .map(HotelGroup::getHotelGroupCode)
                .toList();
        if (hotelGroupCodes.isEmpty()) return;

        long existing = customerRepository.count();

        // 기본 5000, -Ddummy.customer.count=5000 같은 JVM 옵션으로 변경 가능
        int target = Integer.getInteger("dummy.customer.count", 5000);

        if (existing >= target) return;

        LocalDateTime now = LocalDateTime.now();

        // seq는 phone/email 유니크용이라 existing+1부터 이어가야 안전
        for (long seq = existing + 1; seq <= target; seq++) {
            Long hotelGroupCode = hotelGroupCodes.get((int)((seq - 1) % hotelGroupCodes.size()));
            LocalDateTime createdAt = now.minusDays(randomInt(0, 365));

            createFullCustomerData((int) seq, hotelGroupCode, createdAt);

            if (seq % 200 == 0) {
                em.flush();
                em.clear();
            }
        }
    }

    private void createFullCustomerData(int seq, Long hotelGroupCode, LocalDateTime createdAt) {
        // 1) KMS DEK 생성 (Envelope Encryption)
        DataKey dek = kmsService.generateDataKey();
        byte[] plaintextDek = dek.plaintext();
        byte[] dekEnc = dek.encrypted();


        String kmsKeyId = "kms-key-dev-001";

        // 2) 고객명 AES 암호화
        String customerName = "고객" + seq;
        byte[] customerNameEnc = AesCryptoUtils.encrypt(customerName, plaintextDek);

        // 3) 검색용 해시(HMAC-SHA256) → DB 컬럼이 String이면 HEX로 저장
        String customerNameHashHex = toHex(searchHashService.nameHash(customerName));

        NationalityType nationalityType = pickNationalityType();
        ContractType contractType = pickContractType();

        Customer customer = Customer.createCustomer(
                hotelGroupCode,
                customerNameEnc,
                customerNameHashHex,
                nationalityType,
                contractType,
                kmsKeyId,
                dekEnc,
                createdAt
        );

        customerRepository.save(customer);

        // 4) 상태 변경 + 이력
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

        // 5) 연락처 AES + 해시
        createContacts(customer.getCustomerCode(), seq, plaintextDek, createdAt);

        // 6) member / memo (기존 로직 유지)
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
    }

    private void createContacts(Long customerCode, int seq, byte[] plaintextDek, LocalDateTime createdAt) {
        // PHONE
        String phone = "010" + String.format("%08d", seq);
        byte[] phoneEnc = AesCryptoUtils.encrypt(phone, plaintextDek);
        String phoneHashHex = toHex(searchHashService.phoneHash(phone));

        Boolean phoneOptIn = chance(0.50);
        LocalDateTime phoneConsentAt = phoneOptIn ? createdAt : null;

        customerContactRepository.save(
                CustomerContact.createCustomerContact(
                        customerCode,
                        ContactType.PHONE,
                        phoneEnc,
                        phoneHashHex,
                        Boolean.TRUE,
                        phoneOptIn,
                        phoneConsentAt,
                        createdAt
                )
        );

        // EMAIL
        String email = "dummy" + seq + "@gaekdam.test";
        byte[] emailEnc = AesCryptoUtils.encrypt(email, plaintextDek);
        String emailHashHex = toHex(searchHashService.emailHash(email));

        Boolean emailOptIn = chance(0.50);
        LocalDateTime emailConsentAt = emailOptIn ? createdAt : null;

        customerContactRepository.save(
                CustomerContact.createCustomerContact(
                        customerCode,
                        ContactType.EMAIL,
                        emailEnc,
                        emailHashHex,
                        Boolean.FALSE,
                        emailOptIn,
                        emailConsentAt,
                        createdAt
                )
        );
    }

    // ===== helpers =====

    private boolean chance(double probability) {
        return ThreadLocalRandom.current().nextDouble() < probability;
    }

    private int randomInt(int minInclusive, int maxExclusive) {
        return ThreadLocalRandom.current().nextInt(minInclusive, maxExclusive);
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

    private String toHex(byte[] bytes) {
        if (bytes == null) return null;
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
