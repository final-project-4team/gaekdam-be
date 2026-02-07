package com.gaekdam.gaekdambe.dummy.generate.iam_service.employee;



import com.gaekdam.gaekdambe.global.crypto.*;
import com.gaekdam.gaekdambe.iam_service.employee.command.application.dto.request.EmployeeSecureRegistrationRequest;
import com.gaekdam.gaekdambe.iam_service.employee.command.infrastructure.EmployeeRepository; // count 확인용
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;



@Component
public class EmployeeEncryptedRegistrationTest {

  private final JdbcTemplate jdbcTemplate;
  private final KmsService kmsService;
  private final SearchHashService searchHashService;
  private final PasswordEncoder passwordEncoder;
  private final EmployeeRepository employeeRepository; // 중복 생성 방지용

  private static final int BATCH_SIZE = 500;
  private static final long DEPARTMENT_NUM = 8L;
  private static final long HOTEL_POSITION_NUM = 2L;

  public EmployeeEncryptedRegistrationTest(JdbcTemplate jdbcTemplate, KmsService kmsService,
      SearchHashService searchHashService, PasswordEncoder passwordEncoder,
      EmployeeRepository employeeRepository) {
    this.jdbcTemplate = jdbcTemplate;
    this.kmsService = kmsService;
    this.searchHashService = searchHashService;
    this.passwordEncoder = passwordEncoder;
    this.employeeRepository = employeeRepository;
  }

  @Transactional
  public void generate() {
    // 1. 이미 데이터가 있으면 생성하지 않음
    if (employeeRepository.count() > 0) {
      return;
    }



    // 2. [최적화] 무거운 연산(KMS, BCrypt)은 루프 밖에서 1번만 수행
    // 모든 더미 데이터가 동일한 비밀번호와 DEK를 공유함 (테스트 속도 향상 목적)
    String sharedPasswordHash = passwordEncoder.encode("!password123");

    DataKey sharedDek = kmsService.generateDataKey();
    byte[] dekPlain = sharedDek.plaintext();
    byte[] dekEncrypted = sharedDek.encrypted();

    List<EmployeeSecureRegistrationRequest> buffer = new ArrayList<>();

    // 3. 5000명 데이터 생성 루프
    for (long i = 0; i < 5000; i++) {
      long hotelGroupCode = (i / 1000) + 1L; // 1000명마다 그룹 변경 (1~5)

      // 데이터 생성 로직 복구
      long employeeNumber = 10001L + i;
      // propertyCode: 호텔 그룹당 5개의 프로퍼티가 있다고 가정
      long propertyCode = (hotelGroupCode - 1) * 5 + (long) (Math.random() * 5) + 1;

      String loginId = "employee" + i;
      String email = "employee" + i + ".test@company.com";
      String phone = String.format("010-1234-%04d", i);
      String name = "홍길동" + i;


      long deptCode;
      long posPermCode;

      if (i % 1000 == 0) {
        // [대표/임원] 각 호텔의 '첫 번째' 직원
        // 무조건 해당 호텔 그룹의 '첫 번째 부서' 배정
        deptCode = (hotelGroupCode - 1) * DEPARTMENT_NUM + 1;

        // 권한 ID 계산: (부서-1)*2 + 1(첫번째슬롯) + 1(DB밀림보정)
        // 호텔1 -> (0)*2 + 2 = 2
        // 호텔2 -> (8)*2 + 2 = 18
        // 호텔3 -> (16)*2 + 2 = 34
        posPermCode = (deptCode - 1) * HOTEL_POSITION_NUM + 1 + 1;

        name = "대표이사" + hotelGroupCode;
      }
      else {
        // [일반 직원]
        // 해당 호텔 그룹의 '2번째 ~ 8번째' 부서 중에서만 랜덤 배정 (1번 부서 제외)
        // random(7) -> 0~6. 여기에 +2를 하면 2~8이 됨.
        long randomDeptOffset = (long)(Math.random() * (DEPARTMENT_NUM - 1)) + 2;
        deptCode = (hotelGroupCode - 1) * DEPARTMENT_NUM + randomDeptOffset;

        // 권한 ID 계산: (부서-1)*2 + 랜덤슬롯(1or2) + 1(DB밀림보정)
        // 결과 범위: 4 ~ 17 (정확함)
        long randomSlot = (long)(Math.random() * HOTEL_POSITION_NUM) + 1; // 1 or 2
        posPermCode = (deptCode - 1) * HOTEL_POSITION_NUM + randomSlot + 1;
      }
      EmployeeSecureRegistrationRequest request = new EmployeeSecureRegistrationRequest(
          employeeNumber, // DTO가 String이라면 변환
          loginId,
          "!password123", // 실제론 위에서 만든 sharedPasswordHash 사용
          email,
          phone,
          name,
          deptCode,
          posPermCode, // PositionCode
          propertyCode,
          posPermCode // PermissionCode (테스트상 Position과 동일하게 처리한듯 함)
      );

      buffer.add(request);

      // 4. 버퍼가 차면 DB에 Insert
      if (buffer.size() == BATCH_SIZE) {
        batchInsert(hotelGroupCode, buffer, sharedPasswordHash, dekPlain, dekEncrypted);
        buffer.clear();
      }
    }

    // 5. 남은 버퍼 처리
    if (!buffer.isEmpty()) {
      long lastHotelGroupCode = (4999 / 1000) + 1L;
      batchInsert(lastHotelGroupCode, buffer, sharedPasswordHash, dekPlain, dekEncrypted);
    }

  }

  private void batchInsert(Long hotelGroupCode, List<EmployeeSecureRegistrationRequest> batchList,
      String passwordHash, byte[] dekPlain, byte[] dekEncrypted) {

    // 엔티티 정의에 맞게 컬럼명 전면 수정
    String sql = "INSERT INTO employee (" +
        "`employee_number`, `login_id`, `password_hash`, " + // password -> password_hash
        "`email_enc`, `phone_number_enc`, `employee_name_enc`, " + // _enc 접미사 추가
        "`email_hash`, `phone_number_hash`, `employee_name_hash`, " + // name_hash -> employee_name_hash
        "`dek_enc`, `hired_at`, `created_at`, `updated_at`, " + // data_key -> dek_enc, hired_at/updated_at 추가
        "`employee_status`, `failed_login_count`, " + // 필수값 추가
        "`department_code`, `hotel_position_code`, `property_code`, `permission_code`, `hotel_group_code`" + // position_code -> hotel_position_code
        ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
      @Override
      public void setValues(PreparedStatement ps, int i) throws SQLException {
        EmployeeSecureRegistrationRequest req = batchList.get(i);

        // 암호화 (CPU 연산)
        byte[] emailEnc = (req.email() != null) ? AesCryptoUtils.encrypt(req.email(), dekPlain) : null;
        byte[] phoneEnc = AesCryptoUtils.encrypt(req.phoneNumber(), dekPlain);
        byte[] nameEnc = AesCryptoUtils.encrypt(req.name(), dekPlain);

        // 해싱
        byte[] emailHash = (req.email() != null) ? searchHashService.emailHash(req.email()) : null;
        byte[] phoneHash = searchHashService.phoneHash(req.phoneNumber());
        byte[] nameHash = searchHashService.nameHash(req.name());

        LocalDateTime now = LocalDateTime.now();

        // 1. 기본 정보
        ps.setLong(1, req.employeeNumber()); // DB 타입이 Long이므로 변환
        ps.setString(2, req.loginId());
        ps.setString(3, passwordHash);

        // 2. 암호화된 개인정보 (컬럼명: email_enc, phone_number_enc, employee_name_enc)
        setBytesOrNull(ps, 4, emailEnc); // email_enc
        ps.setBytes(5, phoneEnc);        // phone_number_enc
        ps.setBytes(6, nameEnc);         // employee_name_enc

        // 3. 해시 데이터 (컬럼명: email_hash, phone_number_hash, employee_name_hash)
        setBytesOrNull(ps, 7, emailHash);
        ps.setBytes(8, phoneHash);
        ps.setBytes(9, nameHash);

        // 4. 메타 데이터 및 필수 날짜/상태값
        ps.setBytes(10, dekEncrypted);                 // dek_enc
        ps.setTimestamp(11, Timestamp.valueOf(now));   // hired_at (입사일, 임시로 현재시간)
        ps.setTimestamp(12, Timestamp.valueOf(now));   // created_at
        ps.setTimestamp(13, Timestamp.valueOf(now));   // updated_at
        ps.setString(14, "ACTIVE");                    // employee_status (Enum String)
        ps.setInt(15, 0);                              // failed_login_count (int)

        // 5. 외래 키 (FK)
        ps.setLong(16, req.departmentCode());          // department_code
        ps.setLong(17, req.positionCode());            // hotel_position_code (주의: position_code 아님)
        ps.setLong(18, req.propertyCode());            // property_code
        ps.setLong(19, req.permissionCode());          // permission_code
        ps.setLong(20, hotelGroupCode);                // hotel_group_code
      }

      @Override
      public int getBatchSize() {
        return batchList.size();
      }
    });
  }

  private void setBytesOrNull(PreparedStatement ps, int index, byte[] value) throws SQLException {
    if (value != null) {
      ps.setBytes(index, value);
    } else {
      ps.setNull(index, Types.VARBINARY);
    }
  }
}