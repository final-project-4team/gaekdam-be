package com.gaekdam.gaekdambe.unit.iam_service.log.query.service;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.entity.Customer;
import com.gaekdam.gaekdambe.customer_service.customer.command.infrastructure.repository.CustomerRepository;
import com.gaekdam.gaekdambe.global.crypto.AesCryptoUtils;
import com.gaekdam.gaekdambe.global.crypto.KmsService;
import com.gaekdam.gaekdambe.global.crypto.MaskingUtils;
import com.gaekdam.gaekdambe.global.crypto.SearchHashService;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.iam_service.employee.command.domain.entity.Employee;
import com.gaekdam.gaekdambe.iam_service.employee.command.infrastructure.EmployeeRepository;
import com.gaekdam.gaekdambe.iam_service.log.query.dto.request.PersonalInformationLogSearchRequest;
import com.gaekdam.gaekdambe.iam_service.log.query.dto.response.PersonalInformationLogQueryResponse;
import com.gaekdam.gaekdambe.iam_service.log.query.mapper.PersonalInformationLogMapper;
import com.gaekdam.gaekdambe.iam_service.log.query.service.PersonalInformationLogQueryService;
import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.seeds.PermissionTypeKey;
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
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PersonalInformationLogQueryServiceTest {

        @InjectMocks
        private PersonalInformationLogQueryService service;

        @Mock
        private PersonalInformationLogMapper personalInformationLogMapper;
        @Mock
        private EmployeeRepository employeeRepository;
        @Mock
        private CustomerRepository customerRepository;
        @Mock
        private KmsService kmsService;
        @Mock
        private SearchHashService searchHashService;

        @Test
        @DisplayName("getPersonalInformationLogs: 검색 해시 적용 및 복호화/마스킹 검증")
        void getPersonalInformationLogs_success() {
                // given
                Long hgCode = 1L;
                Long empCode = 10L;
                Long targetCode = 20L;
                PersonalInformationLogSearchRequest searchReq = new PersonalInformationLogSearchRequest(
                                hgCode, "loginId", "permType", "Purpose", null, "AccessorName", "EMPLOYEE", targetCode,
                                "TargetName",
                                null, null);

                given(searchHashService.nameHash("AccessorName")).willReturn(new byte[] { 1 });
                given(searchHashService.nameHash("TargetName")).willReturn(new byte[] { 2 });

                PersonalInformationLogQueryResponse item = new PersonalInformationLogQueryResponse(
                                1L, LocalDateTime.now(), PermissionTypeKey.EMPLOYEE_READ,
                                empCode, "EmpEnc", "empId",
                                "EMPLOYEE", targetCode, "TargetEnc", "Purpose");

                given(personalInformationLogMapper.findPersonalInformationLogs(eq(hgCode), any(), any(),
                                eq(new byte[] { 1 }),
                                eq(new byte[] { 2 }), any()))
                                .willReturn(List.of(item));
                given(personalInformationLogMapper.countPersonalInformationLogs(eq(hgCode), any(), eq(new byte[] { 1 }),
                                eq(new byte[] { 2 })))
                                .willReturn(1L);

                // Mock Employee (Accessor)
                Employee accessor = Mockito.mock(Employee.class);
                given(employeeRepository.findById(empCode)).willReturn(Optional.of(accessor));
                given(accessor.getDekEnc()).willReturn(new byte[] { 10 });
                given(accessor.getEmployeeNameEnc()).willReturn(new byte[] { 11 });

                // Mock Employee (Target)
                Employee target = Mockito.mock(Employee.class);
                given(employeeRepository.findById(targetCode)).willReturn(Optional.of(target));
                given(target.getDekEnc()).willReturn(new byte[] { 20 });
                given(target.getEmployeeNameEnc()).willReturn(new byte[] { 21 });

                given(kmsService.decryptDataKey(any())).willReturn(new byte[] { 99 });

                try (MockedStatic<AesCryptoUtils> mockedAes = Mockito.mockStatic(AesCryptoUtils.class);
                                MockedStatic<MaskingUtils> mockedMask = Mockito.mockStatic(MaskingUtils.class)) {

                        mockedAes.when(() -> AesCryptoUtils.decrypt(eq(new byte[] { 11 }), eq(new byte[] { 99 })))
                                        .thenReturn("AccessorReal");
                        mockedAes.when(() -> AesCryptoUtils.decrypt(eq(new byte[] { 21 }), eq(new byte[] { 99 })))
                                        .thenReturn("TargetReal");

                        mockedMask.when(() -> MaskingUtils.maskName("AccessorReal")).thenReturn("AccessorMasked");
                        mockedMask.when(() -> MaskingUtils.maskName("TargetReal")).thenReturn("TargetMasked");

                        // when
                        PageResponse<PersonalInformationLogQueryResponse> response = service.getPersonalInformationLogs(
                                        hgCode, new PageRequest(), searchReq, new SortRequest());

                        // then
                        assertThat(response.getContent().get(0).employeeAccessorName()).isEqualTo("AccessorMasked");
                        assertThat(response.getContent().get(0).targetName()).isEqualTo("TargetMasked");
                }
        }

        // ========== decryptEmployeeName Tests ==========

        @Test
        @DisplayName("decryptEmployeeName: employeeCode가 null이면 fallback 반환")
        void decryptEmployeeName_nullCode_returnsFallback() {
                // This tests the private method indirectly via decryptTargetName
                // given
                PersonalInformationLogQueryResponse dto = new PersonalInformationLogQueryResponse(
                                1L, LocalDateTime.now(), PermissionTypeKey.EMPLOYEE_READ,
                                10L, "AccessorEnc", "accessorId",
                                "EMPLOYEE", null, "FallbackName", "Purpose");

                // when - decryptTargetName calls decryptEmployeeName internally
                // We need to use reflection or test via public method
                // For simplicity, we test via the main flow in getPersonalInformationLogs

                given(personalInformationLogMapper.findPersonalInformationLogs(any(), any(), any(), any(), any(),
                                any()))
                                .willReturn(List.of(dto));
                given(personalInformationLogMapper.countPersonalInformationLogs(any(), any(), any(), any()))
                                .willReturn(1L);

                Employee accessor = Mockito.mock(Employee.class);
                given(employeeRepository.findById(10L)).willReturn(Optional.of(accessor));
                given(accessor.getDekEnc()).willReturn(new byte[] { 10 });
                given(accessor.getEmployeeNameEnc()).willReturn(new byte[] { 11 });
                given(kmsService.decryptDataKey(any())).willReturn(new byte[] { 99 });

                try (MockedStatic<AesCryptoUtils> mockedAes = Mockito.mockStatic(AesCryptoUtils.class);
                                MockedStatic<MaskingUtils> mockedMask = Mockito.mockStatic(MaskingUtils.class)) {

                        mockedAes.when(() -> AesCryptoUtils.decrypt(any(), any())).thenReturn("Decrypted");
                        mockedMask.when(() -> MaskingUtils.maskName(any())).thenAnswer(inv -> inv.getArgument(0));

                        // when
                        PageResponse<PersonalInformationLogQueryResponse> response = service.getPersonalInformationLogs(
                                        1L, new PageRequest(),
                                        new PersonalInformationLogSearchRequest(null, null, null, null, null, null,
                                                        null, null, null, null, null),
                                        new SortRequest());

                        // then - targetName should be fallback since targetCode is null
                        assertThat(response.getContent().get(0).targetName()).isEqualTo("FallbackName");
                }
        }

        @Test
        @DisplayName("decryptEmployeeName: Employee를 찾지 못하면 fallback 반환")
        void decryptEmployeeName_employeeNotFound_returnsFallback() {
                // given
                Long nonExistentCode = 999L;
                PersonalInformationLogQueryResponse dto = new PersonalInformationLogQueryResponse(
                                1L, LocalDateTime.now(), PermissionTypeKey.EMPLOYEE_READ,
                                10L, "AccessorEnc", "accessorId",
                                "EMPLOYEE", nonExistentCode, "FallbackName", "Purpose");

                given(personalInformationLogMapper.findPersonalInformationLogs(any(), any(), any(), any(), any(),
                                any()))
                                .willReturn(List.of(dto));
                given(personalInformationLogMapper.countPersonalInformationLogs(any(), any(), any(), any()))
                                .willReturn(1L);

                Employee accessor = Mockito.mock(Employee.class);
                given(employeeRepository.findById(10L)).willReturn(Optional.of(accessor));
                given(employeeRepository.findById(nonExistentCode)).willReturn(Optional.empty());
                given(accessor.getDekEnc()).willReturn(new byte[] { 10 });
                given(accessor.getEmployeeNameEnc()).willReturn(new byte[] { 11 });
                given(kmsService.decryptDataKey(any())).willReturn(new byte[] { 99 });

                try (MockedStatic<AesCryptoUtils> mockedAes = Mockito.mockStatic(AesCryptoUtils.class);
                                MockedStatic<MaskingUtils> mockedMask = Mockito.mockStatic(MaskingUtils.class)) {

                        mockedAes.when(() -> AesCryptoUtils.decrypt(any(), any())).thenReturn("Decrypted");
                        mockedMask.when(() -> MaskingUtils.maskName(any())).thenAnswer(inv -> inv.getArgument(0));

                        // when
                        PageResponse<PersonalInformationLogQueryResponse> response = service.getPersonalInformationLogs(
                                        1L, new PageRequest(),
                                        new PersonalInformationLogSearchRequest(null, null, null, null, null, null,
                                                        null, null, null, null, null),
                                        new SortRequest());

                        // then
                        assertThat(response.getContent().get(0).targetName()).isEqualTo("FallbackName");
                }
        }

        @Test
        @DisplayName("decryptEmployeeName: 복호화 중 예외 발생 시 fallback 반환")
        void decryptEmployeeName_decryptionException_returnsFallback() {
                // given
                Long targetCode = 20L;
                PersonalInformationLogQueryResponse dto = new PersonalInformationLogQueryResponse(
                                1L, LocalDateTime.now(), PermissionTypeKey.EMPLOYEE_READ,
                                10L, "AccessorEnc", "accessorId",
                                "EMPLOYEE", targetCode, "FallbackName", "Purpose");

                given(personalInformationLogMapper.findPersonalInformationLogs(any(), any(), any(), any(), any(),
                                any()))
                                .willReturn(List.of(dto));
                given(personalInformationLogMapper.countPersonalInformationLogs(any(), any(), any(), any()))
                                .willReturn(1L);

                Employee accessor = Mockito.mock(Employee.class);
                Employee target = Mockito.mock(Employee.class);
                given(employeeRepository.findById(10L)).willReturn(Optional.of(accessor));
                given(employeeRepository.findById(targetCode)).willReturn(Optional.of(target));
                given(accessor.getDekEnc()).willReturn(new byte[] { 10 });
                given(accessor.getEmployeeNameEnc()).willReturn(new byte[] { 11 });

                // Throw exception during target decryption
                given(target.getDekEnc()).willThrow(new RuntimeException("Decryption error"));
                given(kmsService.decryptDataKey(eq(new byte[] { 10 }))).willReturn(new byte[] { 99 });

                try (MockedStatic<AesCryptoUtils> mockedAes = Mockito.mockStatic(AesCryptoUtils.class);
                                MockedStatic<MaskingUtils> mockedMask = Mockito.mockStatic(MaskingUtils.class)) {

                        mockedAes.when(() -> AesCryptoUtils.decrypt(eq(new byte[] { 11 }), any()))
                                        .thenReturn("Decrypted");
                        mockedMask.when(() -> MaskingUtils.maskName(any())).thenAnswer(inv -> inv.getArgument(0));

                        // when
                        PageResponse<PersonalInformationLogQueryResponse> response = service.getPersonalInformationLogs(
                                        1L, new PageRequest(),
                                        new PersonalInformationLogSearchRequest(null, null, null, null, null, null,
                                                        null, null, null, null, null),
                                        new SortRequest());

                        // then - should return fallback due to exception
                        assertThat(response.getContent().get(0).targetName()).isEqualTo("FallbackName");
                }
        }

        // ========== decryptCustomerName Tests ==========

        @Test
        @DisplayName("decryptCustomerName: 복호화 성공")
        void decryptCustomerName_success() {
                // given
                Long hgCode = 1L;
                Long targetCode = 30L;
                PersonalInformationLogQueryResponse dto = new PersonalInformationLogQueryResponse(
                                1L, LocalDateTime.now(), PermissionTypeKey.CUSTOMER_READ,
                                10L, "AccessorEnc", "accessorId",
                                "CUSTOMER", targetCode, "TargetCustomerEnc", "Purpose");

                given(personalInformationLogMapper.findPersonalInformationLogs(any(), any(), any(), any(), any(),
                                any()))
                                .willReturn(List.of(dto));
                given(personalInformationLogMapper.countPersonalInformationLogs(any(), any(), any(), any()))
                                .willReturn(1L);

                Employee accessor = Mockito.mock(Employee.class);
                Customer customer = Mockito.mock(Customer.class);
                given(employeeRepository.findById(10L)).willReturn(Optional.of(accessor));
                given(customerRepository.findById(targetCode)).willReturn(Optional.of(customer));

                // Accessor setup
                given(accessor.getDekEnc()).willReturn(new byte[] { 10 });
                given(accessor.getEmployeeNameEnc()).willReturn(new byte[] { 11 });

                // Customer setup
                given(customer.getDekEnc()).willReturn(new byte[] { 30 });
                given(customer.getCustomerNameEnc()).willReturn(new byte[] { 31 });

                // Decryption setup
                given(kmsService.decryptDataKey(eq(new byte[] { 10 }))).willReturn(new byte[] { 99 }); // Accessor key
                given(kmsService.decryptDataKey(eq(new byte[] { 30 }))).willReturn(new byte[] { 88 }); // Customer key

                try (MockedStatic<AesCryptoUtils> mockedAes = Mockito.mockStatic(AesCryptoUtils.class);
                                MockedStatic<MaskingUtils> mockedMask = Mockito.mockStatic(MaskingUtils.class)) {

                        // Accessor decryption
                        mockedAes.when(() -> AesCryptoUtils.decrypt(eq(new byte[] { 11 }), eq(new byte[] { 99 })))
                                        .thenReturn("AccessorReal");
                        // Customer decryption
                        mockedAes.when(() -> AesCryptoUtils.decrypt(eq(new byte[] { 31 }), eq(new byte[] { 88 })))
                                        .thenReturn("CustomerReal");

                        mockedMask.when(() -> MaskingUtils.maskName("AccessorReal")).thenReturn("AccessorMasked");
                        mockedMask.when(() -> MaskingUtils.maskName("CustomerReal")).thenReturn("CustomerMasked");

                        // when
                        PageResponse<PersonalInformationLogQueryResponse> response = service.getPersonalInformationLogs(
                                        hgCode, new PageRequest(),
                                        new PersonalInformationLogSearchRequest(null, null, null, null, null, null,
                                                        null, null, null, null, null),
                                        new SortRequest());

                        // then
                        assertThat(response.getContent().get(0).targetName()).isEqualTo("CustomerMasked");

                        // Verify decryption calls
                        verify(kmsService).decryptDataKey(eq(new byte[] { 30 }));
                }
        }

        @Test
        @DisplayName("decryptCustomerName: customerCode가 null이면 fallback 반환")
        void decryptCustomerName_nullCode_returnsFallback() {
                // given
                PersonalInformationLogQueryResponse dto = new PersonalInformationLogQueryResponse(
                                1L, LocalDateTime.now(), PermissionTypeKey.CUSTOMER_READ,
                                10L, "AccessorEnc", "accessorId",
                                "CUSTOMER", null, "FallbackCustomer", "Purpose");

                given(personalInformationLogMapper.findPersonalInformationLogs(any(), any(), any(), any(), any(),
                                any()))
                                .willReturn(List.of(dto));
                given(personalInformationLogMapper.countPersonalInformationLogs(any(), any(), any(), any()))
                                .willReturn(1L);

                Employee accessor = Mockito.mock(Employee.class);
                given(employeeRepository.findById(10L)).willReturn(Optional.of(accessor));
                given(accessor.getDekEnc()).willReturn(new byte[] { 10 });
                given(accessor.getEmployeeNameEnc()).willReturn(new byte[] { 11 });
                given(kmsService.decryptDataKey(any())).willReturn(new byte[] { 99 });

                try (MockedStatic<AesCryptoUtils> mockedAes = Mockito.mockStatic(AesCryptoUtils.class);
                                MockedStatic<MaskingUtils> mockedMask = Mockito.mockStatic(MaskingUtils.class)) {

                        mockedAes.when(() -> AesCryptoUtils.decrypt(any(), any())).thenReturn("Decrypted");
                        mockedMask.when(() -> MaskingUtils.maskName(any())).thenAnswer(inv -> inv.getArgument(0));

                        // when
                        PageResponse<PersonalInformationLogQueryResponse> response = service.getPersonalInformationLogs(
                                        1L, new PageRequest(),
                                        new PersonalInformationLogSearchRequest(null, null, null, null, null, null,
                                                        null, null, null, null, null),
                                        new SortRequest());

                        // then
                        assertThat(response.getContent().get(0).targetName()).isEqualTo("FallbackCustomer");
                }
        }

        @Test
        @DisplayName("decryptCustomerName: Customer를 찾지 못하면 fallback 반환")
        void decryptCustomerName_customerNotFound_returnsFallback() {
                // given
                Long nonExistentCode = 999L;
                PersonalInformationLogQueryResponse dto = new PersonalInformationLogQueryResponse(
                                1L, LocalDateTime.now(), PermissionTypeKey.CUSTOMER_READ,
                                10L, "AccessorEnc", "accessorId",
                                "CUSTOMER", nonExistentCode, "FallbackCustomer", "Purpose");

                given(personalInformationLogMapper.findPersonalInformationLogs(any(), any(), any(), any(), any(),
                                any()))
                                .willReturn(List.of(dto));
                given(personalInformationLogMapper.countPersonalInformationLogs(any(), any(), any(), any()))
                                .willReturn(1L);

                Employee accessor = Mockito.mock(Employee.class);
                given(employeeRepository.findById(10L)).willReturn(Optional.of(accessor));
                given(customerRepository.findById(nonExistentCode)).willReturn(Optional.empty());
                given(accessor.getDekEnc()).willReturn(new byte[] { 10 });
                given(accessor.getEmployeeNameEnc()).willReturn(new byte[] { 11 });
                given(kmsService.decryptDataKey(any())).willReturn(new byte[] { 99 });

                try (MockedStatic<AesCryptoUtils> mockedAes = Mockito.mockStatic(AesCryptoUtils.class);
                                MockedStatic<MaskingUtils> mockedMask = Mockito.mockStatic(MaskingUtils.class)) {

                        mockedAes.when(() -> AesCryptoUtils.decrypt(any(), any())).thenReturn("Decrypted");
                        mockedMask.when(() -> MaskingUtils.maskName(any())).thenAnswer(inv -> inv.getArgument(0));

                        // when
                        PageResponse<PersonalInformationLogQueryResponse> response = service.getPersonalInformationLogs(
                                        1L, new PageRequest(),
                                        new PersonalInformationLogSearchRequest(null, null, null, null, null, null,
                                                        null, null, null, null, null),
                                        new SortRequest());

                        // then
                        assertThat(response.getContent().get(0).targetName()).isEqualTo("FallbackCustomer");
                }
        }

        @Test
        @DisplayName("decryptCustomerName: 복호화 중 예외 발생 시 fallback 반환")
        void decryptCustomerName_decryptionException_returnsFallback() {
                // given
                Long targetCode = 30L;
                PersonalInformationLogQueryResponse dto = new PersonalInformationLogQueryResponse(
                                1L, LocalDateTime.now(), PermissionTypeKey.CUSTOMER_READ,
                                10L, "AccessorEnc", "accessorId",
                                "CUSTOMER", targetCode, "FallbackCustomer", "Purpose");

                given(personalInformationLogMapper.findPersonalInformationLogs(any(), any(), any(), any(), any(),
                                any()))
                                .willReturn(List.of(dto));
                given(personalInformationLogMapper.countPersonalInformationLogs(any(), any(), any(), any()))
                                .willReturn(1L);

                Employee accessor = Mockito.mock(Employee.class);
                Customer customer = Mockito.mock(Customer.class);
                given(employeeRepository.findById(10L)).willReturn(Optional.of(accessor));
                given(customerRepository.findById(targetCode)).willReturn(Optional.of(customer));
                given(accessor.getDekEnc()).willReturn(new byte[] { 10 });
                given(accessor.getEmployeeNameEnc()).willReturn(new byte[] { 11 });

                // Throw exception during customer decryption
                given(customer.getDekEnc()).willThrow(new RuntimeException("Decryption error"));
                given(kmsService.decryptDataKey(eq(new byte[] { 10 }))).willReturn(new byte[] { 99 });

                try (MockedStatic<AesCryptoUtils> mockedAes = Mockito.mockStatic(AesCryptoUtils.class);
                                MockedStatic<MaskingUtils> mockedMask = Mockito.mockStatic(MaskingUtils.class)) {

                        mockedAes.when(() -> AesCryptoUtils.decrypt(eq(new byte[] { 11 }), any()))
                                        .thenReturn("Decrypted");
                        mockedMask.when(() -> MaskingUtils.maskName(any())).thenAnswer(inv -> inv.getArgument(0));

                        // when
                        PageResponse<PersonalInformationLogQueryResponse> response = service.getPersonalInformationLogs(
                                        1L, new PageRequest(),
                                        new PersonalInformationLogSearchRequest(null, null, null, null, null, null,
                                                        null, null, null, null, null),
                                        new SortRequest());

                        // then
                        assertThat(response.getContent().get(0).targetName()).isEqualTo("FallbackCustomer");
                }
        }

        // ========== decryptTargetName Tests ==========

        @Test
        @DisplayName("decryptTargetName: targetType이 EMPLOYEE가 아니고 CUSTOMER도 아니면 targetName 그대로 반환")
        void decryptTargetName_otherType_returnsOriginalName() {
                // given
                PersonalInformationLogQueryResponse dto = new PersonalInformationLogQueryResponse(
                                1L, LocalDateTime.now(), PermissionTypeKey.CUSTOMER_READ,
                                10L, "AccessorEnc", "accessorId",
                                "OTHER_TYPE", 20L, "OriginalTargetName", "Purpose");

                given(personalInformationLogMapper.findPersonalInformationLogs(any(), any(), any(), any(), any(),
                                any()))
                                .willReturn(List.of(dto));
                given(personalInformationLogMapper.countPersonalInformationLogs(any(), any(), any(), any()))
                                .willReturn(1L);

                Employee accessor = Mockito.mock(Employee.class);
                given(employeeRepository.findById(10L)).willReturn(Optional.of(accessor));
                given(accessor.getDekEnc()).willReturn(new byte[] { 10 });
                given(accessor.getEmployeeNameEnc()).willReturn(new byte[] { 11 });
                given(kmsService.decryptDataKey(any())).willReturn(new byte[] { 99 });

                try (MockedStatic<AesCryptoUtils> mockedAes = Mockito.mockStatic(AesCryptoUtils.class);
                                MockedStatic<MaskingUtils> mockedMask = Mockito.mockStatic(MaskingUtils.class)) {

                        mockedAes.when(() -> AesCryptoUtils.decrypt(any(), any())).thenReturn("Decrypted");
                        mockedMask.when(() -> MaskingUtils.maskName(any())).thenAnswer(inv -> inv.getArgument(0));

                        // when
                        PageResponse<PersonalInformationLogQueryResponse> response = service.getPersonalInformationLogs(
                                        1L, new PageRequest(),
                                        new PersonalInformationLogSearchRequest(null, null, null, null, null, null,
                                                        null, null, null, null, null),
                                        new SortRequest());

                        // then - should return original name since type is not EMPLOYEE or CUSTOMER
                        assertThat(response.getContent().get(0).targetName()).isEqualTo("OriginalTargetName");
                }
        }
}
