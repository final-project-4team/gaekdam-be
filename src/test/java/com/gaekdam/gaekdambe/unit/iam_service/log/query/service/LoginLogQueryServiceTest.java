package com.gaekdam.gaekdambe.unit.iam_service.log.query.service;

import com.gaekdam.gaekdambe.global.crypto.AesCryptoUtils;
import com.gaekdam.gaekdambe.global.crypto.KmsService;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.iam_service.employee.command.domain.entity.Employee;
import com.gaekdam.gaekdambe.iam_service.employee.command.infrastructure.EmployeeRepository;
import com.gaekdam.gaekdambe.iam_service.log.command.domain.LoginResult;
import com.gaekdam.gaekdambe.iam_service.log.query.dto.request.LoginLogSearchRequest;
import com.gaekdam.gaekdambe.iam_service.log.query.dto.response.LoginLogQueryResponse;
import com.gaekdam.gaekdambe.iam_service.log.query.mapper.LoginLogMapper;
import com.gaekdam.gaekdambe.iam_service.log.query.service.LoginLogQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class LoginLogQueryServiceTest {

    @InjectMocks
    private LoginLogQueryService service;

    @Mock
    private LoginLogMapper logMapper;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private KmsService kmsService;

    @Test
    @DisplayName("getLoginLogs: 로그인 로그 조회 및 이름 복호화 성공")
    void getLoginLogs_success() {
        // given
        Long hgCode = 1L;
        Long empCode = 10L;
        LoginLogQueryResponse item = new LoginLogQueryResponse(
                1L, "LOGIN_SUCCESS", empCode, "EncName", "loginId", "1.1.1.1", LocalDateTime.now(), LoginResult.SUCCESS,
                null, hgCode);

        given(logMapper.findLoginLogs(eq(hgCode), any(), any(), any())).willReturn(List.of(item));
        given(logMapper.countLoginLogs(eq(hgCode), any())).willReturn(1L);

        Employee emp = Mockito.mock(Employee.class);
        given(employeeRepository.findById(empCode)).willReturn(Optional.of(emp));
        given(emp.getDekEnc()).willReturn(new byte[] { 1 });
        given(emp.getEmployeeNameEnc()).willReturn(new byte[] { 2 });

        given(kmsService.decryptDataKey(any())).willReturn(new byte[] { 9 });

        try (MockedStatic<AesCryptoUtils> mockedAes = Mockito.mockStatic(AesCryptoUtils.class)) {
            mockedAes.when(() -> AesCryptoUtils.decrypt(eq(new byte[] { 2 }), eq(new byte[] { 9 })))
                    .thenReturn("DecryptedName");

            // when
            PageResponse<LoginLogQueryResponse> response = service.getLoginLogs(
                    hgCode, new PageRequest(),
                    new LoginLogSearchRequest(null, null, null, null, null, null, null, null),
                    new SortRequest());

            // then
            assertThat(response.getContent().get(0).employeeName()).isEqualTo("DecryptedName");
        }
    }

    @Test
    @DisplayName("getLoginLogs: 복호화 중 예외 발생 시 원본 이름 반환")
    void getLoginLogs_decryptionError_returnsOriginalName() {
        // given
        Long hgCode = 1L;
        Long empCode = 10L;
        String originalEncName = "EncName";
        LoginLogQueryResponse item = new LoginLogQueryResponse(
                1L, "LOGIN_SUCCESS", empCode, originalEncName, "loginId", "1.1.1.1", LocalDateTime.now(),
                LoginResult.SUCCESS,
                null, hgCode);

        given(logMapper.findLoginLogs(eq(hgCode), any(), any(), any())).willReturn(List.of(item));
        given(logMapper.countLoginLogs(eq(hgCode), any())).willReturn(1L);

        Employee emp = Mockito.mock(Employee.class);
        given(employeeRepository.findById(empCode)).willReturn(Optional.of(emp));
        given(emp.getDekEnc()).willReturn(new byte[] { 1 });

        // KMS decrypt throws exception
        given(kmsService.decryptDataKey(any())).willThrow(new RuntimeException("KMS Error"));

        // when
        PageResponse<LoginLogQueryResponse> response = service.getLoginLogs(
                hgCode, new PageRequest(),
                new LoginLogSearchRequest(null, null, null, null, null, null, null, null),
                new SortRequest());

        // then
        assertThat(response.getContent().get(0).employeeName()).isEqualTo(originalEncName);
    }
}
