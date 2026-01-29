package com.gaekdam.gaekdambe.customer_service.customer.command.application.service;

import com.gaekdam.gaekdambe.customer_service.customer.command.application.dto.request.CustomerMemoCreateRequest;
import com.gaekdam.gaekdambe.customer_service.customer.command.application.dto.request.CustomerMemoUpdateRequest;
import com.gaekdam.gaekdambe.customer_service.customer.command.application.dto.response.CustomerMemoCommandResponse;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.entity.CustomerMemo;
import com.gaekdam.gaekdambe.customer_service.customer.command.infrastructure.repository.CustomerMemoRepository;
import com.gaekdam.gaekdambe.customer_service.customer.command.infrastructure.repository.CustomerRepository;
import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import com.gaekdam.gaekdambe.global.exception.CustomException;
import com.gaekdam.gaekdambe.global.exception.ErrorCode;
import com.gaekdam.gaekdambe.iam_service.employee.command.domain.entity.Employee;
import com.gaekdam.gaekdambe.iam_service.employee.command.infrastructure.EmployeeRepository;
import com.gaekdam.gaekdambe.iam_service.log.command.application.aop.annotation.AuditLog;
import com.gaekdam.gaekdambe.iam_service.log.command.application.service.AuditLogService;
import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.seeds.PermissionTypeKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CustomerMemoCommandService {

  private final CustomerRepository customerRepository;
  private final CustomerMemoRepository customerMemoRepository;
  private final EmployeeRepository employeeRepository;
  private final AuditLogService auditLogService;

  @Transactional
  @AuditLog(details = "'고객 코드: '+ #customerCode+'\n고객 메모 생성 내용 :' + #request.customerMemoContent", type = PermissionTypeKey.CUSTOMER_MEMO_CREATE)
  public CustomerMemoCommandResponse createCustomerMemo(CustomUser user, Long customerCode,
      CustomerMemoCreateRequest request) {
    validateCustomerScope(user, customerCode);

    Long employeeCode = getEmployeeCode(user);

    CustomerMemo memo = CustomerMemo.registerCustomerMemo(
        customerCode,
        employeeCode,
        request.getCustomerMemoContent(),
        LocalDateTime.now()
    );

    Long memoCode = customerMemoRepository.save(memo).getCustomerMemoCode();
    return new CustomerMemoCommandResponse(memoCode);
  }

  @Transactional
  public CustomerMemoCommandResponse updateCustomerMemo(CustomUser user, Long customerCode,
      Long memoCode, CustomerMemoUpdateRequest request) {
    validateCustomerScope(user, customerCode);

    CustomerMemo memo = customerMemoRepository.findByCustomerMemoCodeAndCustomerCode(memoCode,
            customerCode)
        .orElseThrow(() -> new CustomException(ErrorCode.CUSTOMER_MEMO_NOT_FOUND));

    if (memo.isDeleted()) {
      throw new CustomException(ErrorCode.CUSTOMER_MEMO_NOT_FOUND);
    }

    String prevContent = memo.getCustomerMemoContent();
    memo.changeContent(request.getCustomerMemoContent(), LocalDateTime.now());
    String newContent = memo.getCustomerMemoContent();
    if (!prevContent.equals(newContent)) {
      Long employeeCode = getEmployeeCode(user);
      Employee accessor = employeeRepository.findById(employeeCode).orElse(null);

      //로그 저장
      if (accessor != null) {
        auditLogService.saveAuditLog(
            accessor,
            PermissionTypeKey.CUSTOMER_MEMO_UPDATE,
            prevContent+" -> "+newContent,
            prevContent,
            newContent
        );
      }
    }
    return new CustomerMemoCommandResponse(memo.getCustomerMemoCode());
  }

  @Transactional
  @AuditLog(details = "'고객 코드: '+ #customerCode", type = PermissionTypeKey.CUSTOMER_MEMO_DELETE)
  public void deleteCustomerMemo(CustomUser user, Long customerCode, Long memoCode) {
    validateCustomerScope(user, customerCode);

    CustomerMemo memo = customerMemoRepository.findByCustomerMemoCodeAndCustomerCode(memoCode,
            customerCode)
        .orElseThrow(() -> new CustomException(ErrorCode.CUSTOMER_MEMO_NOT_FOUND));

    if (memo.isDeleted()) {
      return; // 멱등
    }

    Long employeeCode = getEmployeeCode(user);
    memo.delete(employeeCode, LocalDateTime.now());
  }

  private void validateCustomerScope(CustomUser user, Long customerCode) {
    customerRepository.findByCustomerCodeAndHotelGroupCode(customerCode, user.getHotelGroupCode())
        .orElseThrow(() -> new CustomException(ErrorCode.CUSTOMER_NOT_FOUND));
  }

  //  핵심: CustomUser(username) -> Employee -> employeeCode
  private Long getEmployeeCode(CustomUser user) {
    Employee employee = employeeRepository.findByLoginId(user.getUsername())
        .orElseThrow(() -> new CustomException(ErrorCode.INVALID_USER_ID)); // 너희 공통 에러로 처리
    return employee.getEmployeeCode();
  }
}
