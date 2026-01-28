package com.gaekdam.gaekdambe.iam_service.log.query.service;

import com.gaekdam.gaekdambe.global.crypto.AesCryptoUtils;
import com.gaekdam.gaekdambe.global.crypto.KmsService;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.iam_service.employee.command.domain.entity.Employee;
import com.gaekdam.gaekdambe.iam_service.employee.command.infrastructure.EmployeeRepository;
import com.gaekdam.gaekdambe.iam_service.log.query.dto.request.LoginLogSearchRequest;
import com.gaekdam.gaekdambe.iam_service.log.query.dto.response.LoginLogQueryResponse;
import com.gaekdam.gaekdambe.iam_service.log.query.mapper.LoginLogMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginLogQueryService {
  private final LoginLogMapper logMapper;
  private final EmployeeRepository employeeRepository;
  private final KmsService kmsService;

  public PageResponse<LoginLogQueryResponse> getLoginLogs(
      Long hotelGroupCode,
      PageRequest page,
      LoginLogSearchRequest search,
      SortRequest sort) {
    List<LoginLogQueryResponse> list = logMapper.findLoginLogs(hotelGroupCode, page, search, sort);

    // 직원 이름 복호화
    List<LoginLogQueryResponse> decryptedList = list.stream()
        .map(this::decryptEmployeeName)
        .toList();

    long total = logMapper.countLoginLogs(hotelGroupCode,search);

    return new PageResponse<>(
        decryptedList,
        page.getPage(),
        page.getSize(),
        total);
  }

  private LoginLogQueryResponse decryptEmployeeName(LoginLogQueryResponse response) {
    try {
      Employee employee = employeeRepository.findById(response.employeeCode())
          .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

      byte[] plaintextDek = kmsService.decryptDataKey(employee.getDekEnc());
      String decryptedName = AesCryptoUtils.decrypt(employee.getEmployeeNameEnc(), plaintextDek);

      return new LoginLogQueryResponse(
          response.loginLogCode(),
          response.action(),
          response.employeeCode(),
          decryptedName,
          response.loginId(),
          response.userIp(),
          response.occurredAt(),
          response.result(),
          response.failedReason(),
          response.hotelGroupCode());
    } catch (Exception e) {
      return response;
    }
  }
}
