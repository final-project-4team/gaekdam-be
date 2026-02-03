package com.gaekdam.gaekdambe.dummy.generate.iam_service.employee;


import com.gaekdam.gaekdambe.global.crypto.KmsService;
import com.gaekdam.gaekdambe.iam_service.employee.command.application.dto.request.EmployeeSecureRegistrationRequest;
import com.gaekdam.gaekdambe.iam_service.employee.command.application.service.EmployeeSecureRegistrationService;
import com.gaekdam.gaekdambe.iam_service.employee.command.infrastructure.EmployeeRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class EmployeeEncryptedRegistrationTest {
  private static final int BATCH = 500;
  @Autowired
  private EmployeeSecureRegistrationService employeeSecureRegistrationService;

  @Autowired
  private EmployeeRepository employeeRepository;

  @Autowired
  private KmsService kmsService;

  private Long DEPARTMENT_NUM = 8L;
  private Long HOTEL_POSITION_NUM = 2L;

  @Autowired
  EntityManager em;

  @Transactional
  public void generate() {

    if (employeeRepository.count() > 0) {
      return;
    }

    java.util.List<EmployeeSecureRegistrationRequest> buffer = new java.util.ArrayList<>();
    int BATCH_SIZE = 100;

    for (long employeeCount = 0; employeeCount < 5000; employeeCount++) {
      long hotelGroupCode = (employeeCount / 1000) + 1L;
      long employeeNumber = 10001L + employeeCount; // 사번
      String loginId = "hong" + employeeCount; // 로그인ID
      String originalEmail = "hong" + employeeCount + ".gildong@company.com"; // 이메일
      String originalPhone = String.format("010-1234-%04d", employeeCount); // 전화번호
      String originalName = "홍길동" + employeeCount;
      // long count = (long) (Math.random() * 8) + 1;
      long count = (employeeCount % DEPARTMENT_NUM) + 1;//부서 코드
      long random = (count * HOTEL_POSITION_NUM) - (long) (Math.random() * 2);//직급 및 권한 코드

      EmployeeSecureRegistrationRequest command = new EmployeeSecureRegistrationRequest(
          employeeNumber, loginId, "!password123",
          originalEmail, originalPhone, originalName,
          count, random, 1L, random);

      buffer.add(command);

      if (buffer.size() == BATCH_SIZE) {
        employeeSecureRegistrationService.registerEmployees(hotelGroupCode, new java.util.ArrayList<>(buffer));
        em.flush();
        em.clear();
        buffer.clear();
      }
    }

    // 남은 버퍼 처리
    if (!buffer.isEmpty()) {
      long lastIndex = 4999;
      long lastHotelGroupCode = (lastIndex / 1000) + 1L;
      employeeSecureRegistrationService.registerEmployees(lastHotelGroupCode, buffer);
    }
  }
}
