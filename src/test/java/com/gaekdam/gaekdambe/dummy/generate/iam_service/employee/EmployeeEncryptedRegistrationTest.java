package com.gaekdam.gaekdambe.dummy.generate.iam_service.employee;


import com.gaekdam.gaekdambe.global.crypto.KmsService;
import com.gaekdam.gaekdambe.iam_service.employee.command.application.dto.request.EmployeeSecureRegistrationRequest;
import com.gaekdam.gaekdambe.iam_service.employee.command.application.service.EmployeeSecureRegistrationService;
import com.gaekdam.gaekdambe.iam_service.employee.command.infrastructure.EmployeeRepository;
import com.gaekdam.gaekdambe.iam_service.permission.command.domain.entity.Permission;
import com.gaekdam.gaekdambe.iam_service.permission.command.infrastructure.PermissionRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class EmployeeEncryptedRegistrationTest {
  private static final int BATCH_SIZE= 500;
  @Autowired
  private EmployeeSecureRegistrationService employeeSecureRegistrationService;

  @Autowired
  private PermissionRepository permissionRepository;

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


    for (long employeeCount = 0; employeeCount < 5000; employeeCount++) {
      long hotelGroupCode = (employeeCount / 1000) + 1L;
      long employeeNumber = 10001L + employeeCount; // 사번
      long propertyCode= (hotelGroupCode-1)*5+  (long) (Math.random() * 5)+1;
      List<Permission> permissions =
          permissionRepository
              .findByHotelGroup_HotelGroupCode(hotelGroupCode)
              .stream()
              .filter(p -> !p.getPermissionName().equals("초기화 된 권한"))
              .toList();

      if (permissions.isEmpty()) {
        throw new IllegalStateException(
            "No permissions for hotelGroupCode=" + hotelGroupCode
        );
      }
      String loginId = "hong" + employeeCount; // 로그인ID
      String originalEmail = "hong" + employeeCount + ".gildong@company.com"; // 이메일
      String originalPhone = String.format("010-1234-%04d", employeeCount); // 전화번호
      String originalName = "홍길동" + employeeCount;
      long count = (hotelGroupCode - 1) * DEPARTMENT_NUM + (long)(Math.random() * DEPARTMENT_NUM) + 1;//부서 코드
      long random = 1+(count - 1) * HOTEL_POSITION_NUM + (long)(Math.random() * HOTEL_POSITION_NUM) + 1;//직급 및 권한 코드
/*
      Permission randomPermission =
          permissions.get((int)(Math.random() * permissions.size()));
      long permissionCode = randomPermission.getPermissionCode();
*/

      EmployeeSecureRegistrationRequest command = new EmployeeSecureRegistrationRequest(
          employeeNumber, loginId, "!password123",
          originalEmail, originalPhone, originalName,
          count, random, propertyCode, random  );

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
