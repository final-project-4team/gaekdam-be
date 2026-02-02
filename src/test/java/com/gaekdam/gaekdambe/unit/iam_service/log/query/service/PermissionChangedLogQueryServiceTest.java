package com.gaekdam.gaekdambe.unit.iam_service.log.query.service;

import com.gaekdam.gaekdambe.global.crypto.AesCryptoUtils;
import com.gaekdam.gaekdambe.global.crypto.KmsService;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.iam_service.employee.command.domain.entity.Employee;
import com.gaekdam.gaekdambe.iam_service.employee.command.infrastructure.EmployeeRepository;
import com.gaekdam.gaekdambe.iam_service.log.query.dto.request.PermissionChangedLogSearchRequest;
import com.gaekdam.gaekdambe.iam_service.log.query.dto.response.PermissionChangedLogQueryResponse;
import com.gaekdam.gaekdambe.iam_service.log.query.mapper.PermissionChangedLogMapper;
import com.gaekdam.gaekdambe.iam_service.log.query.service.PermissionChangedLogQueryService;
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
class PermissionChangedLogQueryServiceTest {

        @InjectMocks
        private PermissionChangedLogQueryService service;

        @Mock
        private PermissionChangedLogMapper permissionChangedLogMapper;
        @Mock
        private EmployeeRepository employeeRepository;
        @Mock
        private KmsService kmsService;

        @Test
        @DisplayName("getPermissionChangedLogs: 로그 조회 및 복호화 성공")
        void getPermissionChangedLogs_success() {
                // given
                Long hgCode = 1L;
                Long accessorCode = 100L;
                Long changedCode = 200L;

                PermissionChangedLogQueryResponse logItem = new PermissionChangedLogQueryResponse(
                                1L, LocalDateTime.now(),
                                accessorCode, "AccessorEnc", "accId",
                                changedCode, "ChangedEnc", "chgId",
                                hgCode, 10L, "OldPerm", 20L, "NewPerm");

                given(permissionChangedLogMapper.findPermissionChangedLogs(eq(hgCode), any(), any(), any()))
                                .willReturn(List.of(logItem));
                given(permissionChangedLogMapper.countPermissionChangedLogs(eq(hgCode), any())).willReturn(1L);

                // Mock Accessor
                Employee accessor = Mockito.mock(Employee.class);
                given(employeeRepository.findById(accessorCode)).willReturn(Optional.of(accessor));
                given(accessor.getDekEnc()).willReturn(new byte[] { 1 });
                given(accessor.getEmployeeNameEnc()).willReturn(new byte[] { 2 });

                // Mock Changed Employee
                Employee changed = Mockito.mock(Employee.class);
                given(employeeRepository.findById(changedCode)).willReturn(Optional.of(changed));
                given(changed.getDekEnc()).willReturn(new byte[] { 3 });
                given(changed.getEmployeeNameEnc()).willReturn(new byte[] { 4 });

                // Mock KMS & AES
                given(kmsService.decryptDataKey(any())).willReturn(new byte[] { 9 }); // Plain DEK

                try (MockedStatic<AesCryptoUtils> mockedAes = Mockito.mockStatic(AesCryptoUtils.class)) {
                        mockedAes.when(() -> AesCryptoUtils.decrypt(eq(new byte[] { 2 }), eq(new byte[] { 9 })))
                                        .thenReturn("AccessorDecrypted");
                        mockedAes.when(() -> AesCryptoUtils.decrypt(eq(new byte[] { 4 }), eq(new byte[] { 9 })))
                                        .thenReturn("ChangedDecrypted");

                        // when
                        PageResponse<PermissionChangedLogQueryResponse> response = service.getPermissionChangedLogs(
                                        hgCode, new PageRequest(),
                                        new PermissionChangedLogSearchRequest(null, null, null, null, null, null, null),
                                        new SortRequest());

                        // then
                        assertThat(response.getContent()).hasSize(1);
                        PermissionChangedLogQueryResponse result = response.getContent().get(0);
                        assertThat(result.employeeAccessorName()).isEqualTo("AccessorDecrypted");
                        assertThat(result.employeeChangedName()).isEqualTo("ChangedDecrypted");
                }
        }

        @Test
        @DisplayName("getPermissionChangedLogs: 복호화 실패 시 원본 이름 반환")
        void getPermissionChangedLogs_fail_decrypt() {
                // given
                Long hgCode = 1L;
                PermissionChangedLogQueryResponse logItem = new PermissionChangedLogQueryResponse(
                                1L, LocalDateTime.now(),
                                999L, "OriginalName", "loginId",
                                null, null, null,
                                hgCode, 10L, "Old", 20L, "New");

                given(permissionChangedLogMapper.findPermissionChangedLogs(eq(hgCode), any(), any(), any()))
                                .willReturn(List.of(logItem));

                // Employee not found -> decryption skipped
                given(employeeRepository.findById(999L)).willReturn(Optional.empty());

                // when
                PageResponse<PermissionChangedLogQueryResponse> response = service.getPermissionChangedLogs(
                                hgCode, new PageRequest(),
                                new PermissionChangedLogSearchRequest(null, null, null, null, null, null, null),
                                new SortRequest());

                // then
                // Should return "OriginalName" because decryption failed/skipped
                assertThat(response.getContent().get(0).employeeAccessorName()).isEqualTo("OriginalName");
        }

        @Test
        @DisplayName("getPermissionChangedLogs: 복호화 중 예외 발생 시 원본 이름 반환")
        void getPermissionChangedLogs_decryptionError_returnsOriginalName() {
                // given
                Long hgCode = 1L;
                Long accessorCode = 100L;
                Long changedCode = 200L;
                String originalEncName = "AccessorEnc";

                PermissionChangedLogQueryResponse logItem = new PermissionChangedLogQueryResponse(
                                1L, LocalDateTime.now(),
                                accessorCode, originalEncName, "accId",
                                changedCode, "ChangedEnc", "chgId",
                                hgCode, 10L, "OldPerm", 20L, "NewPerm");

                given(permissionChangedLogMapper.findPermissionChangedLogs(eq(hgCode), any(), any(), any()))
                                .willReturn(List.of(logItem));

                // Mock Accessor
                Employee accessor = Mockito.mock(Employee.class);
                given(employeeRepository.findById(accessorCode)).willReturn(Optional.of(accessor));
                given(accessor.getDekEnc()).willReturn(new byte[] { 1 });

                // Mock Changed
                Employee changed = Mockito.mock(Employee.class);
                given(employeeRepository.findById(changedCode)).willReturn(Optional.of(changed));

                // KMS throws exception
                given(kmsService.decryptDataKey(any())).willThrow(new RuntimeException("KMS Error"));

                // when
                PageResponse<PermissionChangedLogQueryResponse> response = service.getPermissionChangedLogs(
                                hgCode, new PageRequest(),
                                new PermissionChangedLogSearchRequest(null, null, null, null, null, null, null),
                                new SortRequest());

                // then
                assertThat(response.getContent().get(0).employeeAccessorName()).isEqualTo(originalEncName);
        }
}
