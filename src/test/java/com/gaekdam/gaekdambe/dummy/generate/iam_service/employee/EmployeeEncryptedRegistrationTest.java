package com.gaekdam.gaekdambe.dummy.generate.iam_service.employee;

import com.gaekdam.gaekdambe.global.crypto.AesCryptoUtils;
import com.gaekdam.gaekdambe.global.crypto.KmsService;
import com.gaekdam.gaekdambe.iam_service.employee.command.application.dto.request.EmployeeSecureRegistrationRequest;
import com.gaekdam.gaekdambe.iam_service.employee.command.application.service.EmployeeSecureRegistrationService;
import com.gaekdam.gaekdambe.iam_service.employee.command.domain.entity.Employee;
import com.gaekdam.gaekdambe.iam_service.employee.command.infrastructure.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Component
public class EmployeeEncryptedRegistrationTest {

    @Autowired
    private EmployeeSecureRegistrationService employeeSecureRegistrationService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private KmsService kmsService;

    @Transactional
    public void generate() {

        if (employeeRepository.count() > 0) {
            return;
        }

        for (long employeeCount = 0; employeeCount < 100; employeeCount++) {
            long employeeNumber = 10001L + employeeCount; // 사번
            String loginId = "hong" + employeeCount; // 로그인ID
            String originalEmail = "hong" + employeeCount + ".gildong@company.com"; // 이메일
            String originalPhone = String.format("010-1234-%04d", employeeCount); // 전화번호
            String originalName = "홍길동" + employeeCount;
            long count = (long) (Math.random() * 8) + 1;

            EmployeeSecureRegistrationRequest command = new EmployeeSecureRegistrationRequest(
                    employeeNumber, loginId, "password123",
                    originalEmail, originalPhone, originalName,
                    count, 2L, 1L, 1L, 1L);

            Long savedEmployeeCode = employeeSecureRegistrationService.registerEmployee(command);
            Employee savedEmployee = employeeRepository.findById(savedEmployeeCode).orElseThrow();

            byte[] plaintextDek = kmsService.decryptDataKey(savedEmployee.getDekEnc());
            String decryptedName = AesCryptoUtils.decrypt(savedEmployee.getEmployeeNameEnc(),
                    plaintextDek);

            assertThat(decryptedName).isEqualTo(originalName);
        }
    }
}
