package com.gaekdam.gaekdambe.dummy.generate.customer_service.customer;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.ChangeSource;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.ContactType;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.ContractType;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.CustomerStatus;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.NationalityType;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.NationalityCode;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.entity.Customer;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.entity.CustomerContact;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.entity.CustomerMemo;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.entity.CustomerStatusHistory;
import com.gaekdam.gaekdambe.customer_service.customer.command.infrastructure.repository.CustomerContactRepository;
import com.gaekdam.gaekdambe.customer_service.customer.command.infrastructure.repository.CustomerMemoRepository;
import com.gaekdam.gaekdambe.customer_service.customer.command.infrastructure.repository.CustomerRepository;
import com.gaekdam.gaekdambe.customer_service.customer.command.infrastructure.repository.CustomerStatusHistoryRepository;
import com.gaekdam.gaekdambe.global.crypto.AesCryptoUtils;
import com.gaekdam.gaekdambe.global.crypto.DataKey;
import com.gaekdam.gaekdambe.global.crypto.HexUtils;
import com.gaekdam.gaekdambe.global.crypto.KmsService;
import com.gaekdam.gaekdambe.global.crypto.SearchHashService;
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

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class DummyCustomerDataTest {

    private static final int TOTAL_CUSTOMERS = 50_000;
    private static final int BATCH = 500;

    // Customer.createCustomer 시그니처가 keyId를 받으니 유지 (LocalKmsService는 내부 masterKey라
    // 실제로 안 씀)
    private static final String KMS_KEY_ID = "dummy-kms-key";

    private static final LocalDateTime START = LocalDateTime.of(2024, 1, 1, 0, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, 12, 31, 23, 59);

    @Autowired
    CustomerRepository customerRepository;
    @Autowired
    CustomerContactRepository contactRepository;
    @Autowired
    CustomerMemoRepository memoRepository;
    @Autowired
    CustomerStatusHistoryRepository statusHistoryRepository;

    @Autowired
    HotelGroupRepository hotelGroupRepository;
    @Autowired
    EmployeeRepository employeeRepository;

    // ✅ 추가: KMS + SearchHash
    @Autowired
    KmsService kmsService;
    @Autowired
    SearchHashService searchHashService;

    @PersistenceContext
    EntityManager em;

    @Transactional
    public void generate() {

        if (customerRepository.count() > 0)
            return;

        List<HotelGroup> hotelGroups = hotelGroupRepository.findAll();
        if (hotelGroups.isEmpty())
            return;

        List<Long> hotelGroupCodes = hotelGroups.stream()
                .map(HotelGroup::getHotelGroupCode)
                .filter(Objects::nonNull)
                .toList();
        if (hotelGroupCodes.isEmpty())
            return;

        Map<Long, List<Long>> employeeCodesByHotelGroup = buildEmployeeCodesByHotelGroup();
        List<Long> fallbackEmployeeCodes = employeeCodesByHotelGroup.values()
                .stream()
                .flatMap(List::stream)
                .distinct()
                .toList();
        if (fallbackEmployeeCodes.isEmpty())
            return;

        Random random = new Random();

        List<Customer> customerBuffer = new ArrayList<>(BATCH);
        List<Integer> indexBuffer = new ArrayList<>(BATCH);
        List<Long> hotelGroupBuffer = new ArrayList<>(BATCH);

        // ✅ 추가: 고객별 plaintext DEK (contact 암호화에 사용)
        List<byte[]> plaintextDekBuffer = new ArrayList<>(BATCH);

        for (int i = 1; i <= TOTAL_CUSTOMERS; i++) {

            LocalDateTime createdAt = randomDateTimeBetween(START, END, random);

            NationalityType nationality = (random.nextInt(100) < 80) ? NationalityType.DOMESTIC
                    : NationalityType.FOREIGN;

            ContractType contractType = (random.nextInt(100) < 85) ? ContractType.INDIVIDUAL : ContractType.CORPORATE;

            long hotelGroupCode = hotelGroupCodes.get((i - 1) % hotelGroupCodes.size());

            String name = makeName(i, nationality, contractType);

            // ✅ KMS에서 DEK 생성
            DataKey dataKey = kmsService.generateDataKey();
            byte[] plaintextDek = dataKey.plaintext();
            byte[] dekEnc = dataKey.encrypted();

            // ✅ nameEnc는 AES 암호문
            byte[] nameEnc = AesCryptoUtils.encrypt(name, plaintextDek);

            // ✅ nameHash는 SearchHashService(HMAC) + Hex 문자열
            String nameHash = HexUtils.toHex(searchHashService.nameHash(name));

            // --- NEW: compute nationality code (enum) to pass into Customer.createCustomer
            // ---
            NationalityCode nationalityCode;
            if (nationality == NationalityType.DOMESTIC) {
                nationalityCode = NationalityCode.KR;
            } else {
                int pick = random.nextInt(100);
                if (pick < 35)
                    nationalityCode = NationalityCode.CN;
                else if (pick < 65)
                    nationalityCode = NationalityCode.JP;
                else if (pick < 90)
                    nationalityCode = NationalityCode.TW;
                else {
                    NationalityCode[] others = new NationalityCode[] {
                            NationalityCode.US, NationalityCode.VN, NationalityCode.TH,
                            NationalityCode.PH, NationalityCode.ID, NationalityCode.IN
                    };
                    nationalityCode = others[random.nextInt(others.length)];
                }
            }

            Customer customer = Customer.createCustomer(
                    hotelGroupCode,
                    nameEnc,
                    nameHash,
                    nationality,
                    nationalityCode,
                    contractType,
                    KMS_KEY_ID,
                    dekEnc,
                    createdAt);

            customerBuffer.add(customer);
            indexBuffer.add(i);
            hotelGroupBuffer.add(hotelGroupCode);
            plaintextDekBuffer.add(plaintextDek);

            if (customerBuffer.size() == BATCH) {
                flushCustomerBatch(
                        customerBuffer, indexBuffer, hotelGroupBuffer, plaintextDekBuffer,
                        employeeCodesByHotelGroup, fallbackEmployeeCodes, random);
            }
        }

        if (!customerBuffer.isEmpty()) {
            flushCustomerBatch(
                    customerBuffer, indexBuffer, hotelGroupBuffer, plaintextDekBuffer,
                    employeeCodesByHotelGroup, fallbackEmployeeCodes, random);
        }
    }

    private Map<Long, List<Long>> buildEmployeeCodesByHotelGroup() {
        List<Employee> employees = employeeRepository.findAll();

        Map<Long, List<Long>> map = new HashMap<>();
        for (Employee e : employees) {
            if (e == null)
                continue;
            if (e.getEmployeeCode() == null)
                continue;
            if (e.getEmployeeStatus() != EmployeeStatus.ACTIVE)
                continue;
            if (e.getHotelGroup() == null || e.getHotelGroup().getHotelGroupCode() == null)
                continue;

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
            List<byte[]> plaintextDekBuffer,
            Map<Long, List<Long>> employeeCodesByHotelGroup,
            List<Long> fallbackEmployeeCodes,
            Random random) {
        customerRepository.saveAll(customerBuffer);
        em.flush();

        List<CustomerContact> contactBuffer = new ArrayList<>(customerBuffer.size() * 2);
        List<CustomerMemo> memoBuffer = new ArrayList<>(customerBuffer.size());
        List<CustomerStatusHistory> statusBuffer = new ArrayList<>(customerBuffer.size());

        for (int i = 0; i < customerBuffer.size(); i++) {
            Customer customer = customerBuffer.get(i);
            int idx = indexBuffer.get(i);
            Long hotelGroupCode = hotelGroupBuffer.get(i);
            byte[] plaintextDek = plaintextDekBuffer.get(i);

            Long customerCode = customer.getCustomerCode();
            LocalDateTime createdAt = customer.getCreatedAt() != null ? customer.getCreatedAt() : LocalDateTime.now();

            Long employeeCode = pickEmployeeCode(hotelGroupCode, employeeCodesByHotelGroup, fallbackEmployeeCodes,
                    random);

            // ✅ PHONE enc/hash
            String phone = makePhone(idx);
            byte[] phoneEnc = AesCryptoUtils.encrypt(phone, plaintextDek);
            String phoneHash = HexUtils.toHex(searchHashService.phoneHash(phone));

            contactBuffer.add(
                    CustomerContact.createCustomerContact(
                            customerCode,
                            ContactType.PHONE,
                            phoneEnc, // ✅ 암호문 저장
                            phoneHash, // ✅ HMAC hash (hex string)
                            true,
                            random.nextInt(100) < 35,
                            createdAt,
                            createdAt));

            // ✅ EMAIL enc/hash
            if (random.nextInt(100) < 65) {
                String email = "user" + idx + "@example.com";
                byte[] emailEnc = AesCryptoUtils.encrypt(email, plaintextDek);
                String emailHash = HexUtils.toHex(searchHashService.emailHash(email));

                contactBuffer.add(
                        CustomerContact.createCustomerContact(
                                customerCode,
                                ContactType.EMAIL,
                                emailEnc, // ✅ 암호문 저장
                                emailHash, // ✅ HMAC hash (hex string)
                                false,
                                random.nextInt(100) < 25,
                                createdAt,
                                createdAt));
            }

            if (random.nextInt(100) < 20) {
                memoBuffer.add(
                        CustomerMemo.registerCustomerMemo(
                                customerCode,
                                employeeCode,
                                "dummy memo for customer " + idx,
                                createdAt));
            }

            if (random.nextInt(100) < 3) {
                LocalDateTime changedAt = createdAt.plusDays(1 + random.nextInt(30));
                if (changedAt.isAfter(END))
                    changedAt = END;

                statusBuffer.add(
                        CustomerStatusHistory.recordCustomerStatusChange(
                                customerCode,
                                CustomerStatus.ACTIVE,
                                CustomerStatus.CAUTION,
                                ChangeSource.SYSTEM,
                                employeeCode,
                                "dummy status history",
                                changedAt));
            }
        }

        if (!contactBuffer.isEmpty())
            contactRepository.saveAll(contactBuffer);
        if (!memoBuffer.isEmpty())
            memoRepository.saveAll(memoBuffer);
        if (!statusBuffer.isEmpty())
            statusHistoryRepository.saveAll(statusBuffer);

        em.flush();
        em.clear();

        customerBuffer.clear();
        indexBuffer.clear();
        hotelGroupBuffer.clear();
        plaintextDekBuffer.clear(); // ✅ 추가
    }

    private Long pickEmployeeCode(
            Long hotelGroupCode,
            Map<Long, List<Long>> employeeCodesByHotelGroup,
            List<Long> fallbackEmployeeCodes,
            Random random) {
        List<Long> list = employeeCodesByHotelGroup.get(hotelGroupCode);
        if (list != null && !list.isEmpty()) {
            return list.get(random.nextInt(list.size()));
        }
        return fallbackEmployeeCodes.get(random.nextInt(fallbackEmployeeCodes.size()));
    }

    private static final String[] KOREAN_LAST_NAMES = {
            "김", "이", "박", "최", "정", "강", "조", "윤", "장", "임", "한", "오", "서", "신", "권", "황", "안", "송", "류", "전", "홍",
            "고", "문", "양", "손", "배", "조", "백", "허", "유", "남", "심", "노", "정", "하", "곽", "성", "차", "주", "우", "구", "신",
            "임", "나", "전", "민", "유", "진", "지", "엄", "채", "원", "천", "방", "공", "강", "현", "함", "변", "염", "양", "변", "여",
            "추", "노", "도", "소", "신", "석", "선", "설", "마", "길", "주", "연", "방", "위", "표", "명", "기", "반", "왕", "금", "옥",
            "육", "인", "맹", "제", "모", "남궁", "탁", "국", "여", "진", "어", "은", "편", "구", "용"
    };

    private static final String[] NAME_SYLLABLES = {
            "민", "준", "서", "예", "지도", "연", "우", "현", "수", "지", "윤", "아", "채", "원", "호", "건", "영", "혁", "재", "승", "하",
            "은", "태", "규", "빈", "나", "다", "라", "마", "바", "사", "환", "결", "율", "솔", "빛", "강", "산", "해", "별", "찬", "담",
            "온", "슬", "제", "훈", "진", "석", "철", "희", "용", "성", "일", "경", "미", "숙", "자", "혜", "영", "주", "선", "정", "교",
            "필", "탁", "학", "형", "기", "보", "명", "동", "광"
    };

    private static final String[] FOREIGN_FIRST_NAMES = {
            "James", "John", "Robert", "Michael", "William", "David", "Richard", "Charles", "Joseph", "Thomas",
            "Mary", "Patricia", "Jennifer", "Linda", "Elizabeth", "Barbara", "Susan", "Jessica", "Sarah", "Karen",
            "Daniel", "Paul", "Mark", "Donald", "George", "Kenneth", "Steven", "Edward", "Brian", "Ronald",
            "Anthony", "Kevin", "Jason", "Matthew", "Gary", "Timothy", "Jose", "Larry", "Jeffrey", "Frank",
            "Scott", "Eric", "Stephen", "Andrew", "Raymond", "Gregory", "Joshua", "Jerry", "Dennis", "Walter",
            "Patrick", "Peter", "Harold", "Douglas", "Henry", "Carl", "Arthur", "Ryan", "Roger", "Joe"
    };

    private static final String[] FOREIGN_LAST_NAMES = {
            "Smith", "Johnson", "Williams", "Jones", "Brown", "Davis", "Miller", "Wilson", "Moore", "Taylor",
            "Anderson", "Thomas", "Jackson", "White", "Harris", "Martin", "Thompson", "Garcia", "Martinez", "Robinson",
            "Clark", "Rodriguez", "Lewis", "Lee", "Walker", "Hall", "Allen", "Young", "Hernandez", "King",
            "Wright", "Lopez", "Hill", "Scott", "Green", "Adams", "Baker", "Gonzalez", "Nelson", "Carter",
            "Mitchell", "Perez", "Roberts", "Turner", "Phillips", "Campbell", "Parker", "Evans", "Edwards", "Collins"
    };

    private static final String[] CORP_PREFIXES = {
            "삼성", "현대", "LG", "SK", "롯데", "포스코", "한화", "GS", "농협", "신세계",
            "KT", "CJ", "한진", "KAKAO", "NAVER", "두산", "LS", "DL", "부영", "중흥",
            "미래에셋", "금호", "S-OIL", "카카오", "네이버", "배달의민족", "쿠팡", "비바리퍼블리카", "야놀자", "무신사",
            "가온", "나누리", "다온", "라온", "마루", "바른", "새롬", "아름", "예쁜", "우리",
            "테크", "솔루션", "시스템", "글로벌", "네트웍스", "이노베이션", "파트너스", "홀딩스", "그룹", "상사"
    };

    private static final String[] CORP_SUFFIXES = {
            "전자", "건설", "물산", "화학", "생명", "증권", "카드", "통신", "유통", "식품",
            "테크", "정보통신", "아이티", "소프트", "엔지니어링", "로지스틱스", "에너지", "정유", "강철", "자동차",
            "주식회사", "(주)", "유한회사", "상사", "개발", "산업", "통상", "무역", "해운", "항공"
    };

    private static String makeName(int idx, NationalityType n, ContractType c) {
        if (c == ContractType.CORPORATE) {
            // 법인 이름 생성
            Random r = new Random();
            String prefix = CORP_PREFIXES[r.nextInt(CORP_PREFIXES.length)];
            String suffix = CORP_SUFFIXES[r.nextInt(CORP_SUFFIXES.length)];
            return prefix + " " + suffix + "_" + idx; // 중복 방지용 idx 살짝 붙임 (혹은 생략 가능)
        }

        if (n == NationalityType.DOMESTIC) {
            // 한국인 이름 생성
            Random r = new Random();
            String last = KOREAN_LAST_NAMES[r.nextInt(KOREAN_LAST_NAMES.length)];
            String first1 = NAME_SYLLABLES[r.nextInt(NAME_SYLLABLES.length)];
            String first2 = NAME_SYLLABLES[r.nextInt(NAME_SYLLABLES.length)];

            // 2글자 이름 (15% 확률) vs 3글자 이름 (85% 확률)
            if (r.nextInt(100) < 15) {
                return last + first1;
            } else {
                return last + first1 + first2;
            }
        } else {
            // 외국인 이름 생성
            Random r = new Random();
            String first = FOREIGN_FIRST_NAMES[r.nextInt(FOREIGN_FIRST_NAMES.length)];
            String last = FOREIGN_LAST_NAMES[r.nextInt(FOREIGN_LAST_NAMES.length)];
            return first + " " + last;
        }
    }

    private static String makePhone(int idx) {
        int mid = (idx % 9000) + 1000;
        int end = ((idx * 7) % 9000) + 1000;
        return "010" + mid + end;
    }

    private static LocalDateTime randomDateTimeBetween(LocalDateTime start, LocalDateTime end, Random random) {
        long seconds = Duration.between(start, end).getSeconds();
        if (seconds <= 0)
            return start;
        long add = (random.nextLong() & Long.MAX_VALUE) % seconds;
        return start.plusSeconds(add);
    }
}
