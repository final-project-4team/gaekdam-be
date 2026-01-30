package com.gaekdam.gaekdambe.unit.customer_service.customer.command.application.service;

import com.gaekdam.gaekdambe.customer_service.customer.command.application.dto.request.CustomerMemoCreateRequest;
import com.gaekdam.gaekdambe.customer_service.customer.command.application.dto.request.CustomerMemoUpdateRequest;
import com.gaekdam.gaekdambe.customer_service.customer.command.application.dto.response.CustomerMemoCommandResponse;
import com.gaekdam.gaekdambe.customer_service.customer.command.application.service.CustomerMemoCommandService;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.entity.Customer;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.entity.CustomerMemo;
import com.gaekdam.gaekdambe.customer_service.customer.command.infrastructure.repository.CustomerMemoRepository;
import com.gaekdam.gaekdambe.customer_service.customer.command.infrastructure.repository.CustomerRepository;
import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import com.gaekdam.gaekdambe.global.exception.CustomException;
import com.gaekdam.gaekdambe.global.exception.ErrorCode;
import com.gaekdam.gaekdambe.iam_service.employee.command.domain.entity.Employee;
import com.gaekdam.gaekdambe.iam_service.employee.command.infrastructure.EmployeeRepository;
import com.gaekdam.gaekdambe.iam_service.log.command.application.service.AuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class CustomerMemoCommandServiceTest {

    private CustomerRepository customerRepository;
    private CustomerMemoRepository customerMemoRepository;
    private EmployeeRepository employeeRepository;
    private AuditLogService auditLogService;

    private CustomerMemoCommandService service;

    @BeforeEach
    void setUp() {
        customerRepository = mock(CustomerRepository.class);
        customerMemoRepository = mock(CustomerMemoRepository.class);
        employeeRepository = mock(EmployeeRepository.class);
        auditLogService = mock(AuditLogService.class);

        service = new CustomerMemoCommandService(customerRepository, customerMemoRepository, employeeRepository,auditLogService);
    }

    private CustomUser mockUser(Long hotelGroupCode) {
        CustomUser user = mock(CustomUser.class);
        given(user.getHotelGroupCode()).willReturn(hotelGroupCode);
        return user;
    }

    private void stubUsername(CustomUser user, String username) {
        given(user.getUsername()).willReturn(username);
    }

    @Test
    @DisplayName("create: 고객 스코프 없으면 CUSTOMER_NOT_FOUND")
    void create_scope_not_found_thenThrow() {
        // given
        CustomUser user = mockUser(1L);
        Long customerCode = 100L;

        CustomerMemoCreateRequest req = mock(CustomerMemoCreateRequest.class); // content stub 불필요

        given(customerRepository.findByCustomerCodeAndHotelGroupCode(customerCode, 1L))
                .willReturn(Optional.empty());

        // when
        CustomException ex = catchThrowableOfType(
                () -> service.createCustomerMemo(user, customerCode, req),
                CustomException.class
        );

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.CUSTOMER_NOT_FOUND);
        then(employeeRepository).shouldHaveNoInteractions();
        then(customerMemoRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("create: user(username)로 employee 못 찾으면 INVALID_USER_ID")
    void create_employee_not_found_thenThrow() {
        // given
        CustomUser user = mockUser(1L);
        stubUsername(user, "hong0");

        Long customerCode = 100L;

        CustomerMemoCreateRequest req = mock(CustomerMemoCreateRequest.class); // content stub 불필요

        given(customerRepository.findByCustomerCodeAndHotelGroupCode(customerCode, 1L))
                .willReturn(Optional.of(mock(Customer.class)));
        given(employeeRepository.findByLoginId("hong0"))
                .willReturn(Optional.empty());

        // when
        CustomException ex = catchThrowableOfType(
                () -> service.createCustomerMemo(user, customerCode, req),
                CustomException.class
        );

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_USER_ID);
        then(customerMemoRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("create: 정상 생성 시 memo 저장되고 customerMemoCode 반환")
    void create_success() {
        // given
        CustomUser user = mockUser(1L);
        stubUsername(user, "hong0");

        Long customerCode = 100L;

        CustomerMemoCreateRequest req = mock(CustomerMemoCreateRequest.class);
        given(req.getCustomerMemoContent()).willReturn("hello"); // 성공 케이스에만 필요

        given(customerRepository.findByCustomerCodeAndHotelGroupCode(customerCode, 1L))
                .willReturn(Optional.of(mock(Customer.class)));

        Employee emp = mock(Employee.class);
        given(employeeRepository.findByLoginId("hong0")).willReturn(Optional.of(emp));
        given(emp.getEmployeeCode()).willReturn(10L);

        CustomerMemo saved = mock(CustomerMemo.class);
        given(saved.getCustomerMemoCode()).willReturn(777L);
        given(customerMemoRepository.save(any(CustomerMemo.class))).willReturn(saved);

        // when
        CustomerMemoCommandResponse res = service.createCustomerMemo(user, customerCode, req);

        // then
        then(customerMemoRepository).should(times(1)).save(any(CustomerMemo.class));
        assertThat(res.getCustomerMemoCode()).isEqualTo(777L);
    }

    @Test
    @DisplayName("update: 고객 스코프 없으면 CUSTOMER_NOT_FOUND")
    void update_scope_not_found_thenThrow() {
        // given
        CustomUser user = mockUser(1L);
        Long customerCode = 100L;
        Long memoCode = 1L;

        CustomerMemoUpdateRequest req = mock(CustomerMemoUpdateRequest.class); // content stub 불필요

        given(customerRepository.findByCustomerCodeAndHotelGroupCode(customerCode, 1L))
                .willReturn(Optional.empty());

        // when
        CustomException ex = catchThrowableOfType(
                () -> service.updateCustomerMemo(user, customerCode, memoCode, req),
                CustomException.class
        );

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.CUSTOMER_NOT_FOUND);
        then(customerMemoRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("update: memo 없으면 CUSTOMER_MEMO_NOT_FOUND")
    void update_memo_not_found_thenThrow() {
        // given
        CustomUser user = mockUser(1L);
        Long customerCode = 100L;
        Long memoCode = 1L;

        CustomerMemoUpdateRequest req = mock(CustomerMemoUpdateRequest.class); // content stub 불필요

        given(customerRepository.findByCustomerCodeAndHotelGroupCode(customerCode, 1L))
                .willReturn(Optional.of(mock(Customer.class)));
        given(customerMemoRepository.findByCustomerMemoCodeAndCustomerCode(memoCode, customerCode))
                .willReturn(Optional.empty());

        // when
        CustomException ex = catchThrowableOfType(
                () -> service.updateCustomerMemo(user, customerCode, memoCode, req),
                CustomException.class
        );

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.CUSTOMER_MEMO_NOT_FOUND);
    }

    @Test
    @DisplayName("update: memo가 deleted면 CUSTOMER_MEMO_NOT_FOUND")
    void update_memo_deleted_thenThrow() {
        // given
        CustomUser user = mockUser(1L);
        Long customerCode = 100L;
        Long memoCode = 1L;

        CustomerMemoUpdateRequest req = mock(CustomerMemoUpdateRequest.class); // content stub 불필요

        given(customerRepository.findByCustomerCodeAndHotelGroupCode(customerCode, 1L))
                .willReturn(Optional.of(mock(Customer.class)));

        CustomerMemo memo = mock(CustomerMemo.class);
        given(customerMemoRepository.findByCustomerMemoCodeAndCustomerCode(memoCode, customerCode))
                .willReturn(Optional.of(memo));
        given(memo.isDeleted()).willReturn(true);

        // when
        CustomException ex = catchThrowableOfType(
                () -> service.updateCustomerMemo(user, customerCode, memoCode, req),
                CustomException.class
        );

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.CUSTOMER_MEMO_NOT_FOUND);
        then(memo).should(never()).changeContent(anyString(), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("update: 정상 수정 시 changeContent 호출되고 customerMemoCode 반환")
    void update_success() {
        // given
        CustomUser user = mockUser(1L);
        Long customerCode = 100L;
        Long memoCode = 1L;

        CustomerMemoUpdateRequest req = mock(CustomerMemoUpdateRequest.class);
        given(req.getCustomerMemoContent()).willReturn("changed"); // 성공 케이스에만 필요

        given(customerRepository.findByCustomerCodeAndHotelGroupCode(customerCode, 1L))
                .willReturn(Optional.of(mock(Customer.class)));

        CustomerMemo memo = mock(CustomerMemo.class);
        given(customerMemoRepository.findByCustomerMemoCodeAndCustomerCode(memoCode, customerCode))
                .willReturn(Optional.of(memo));
        given(memo.isDeleted()).willReturn(false);
        given(memo.getCustomerMemoCode()).willReturn(memoCode);
        given(memo.getCustomerMemoContent()).willReturn("before");

        // when
        CustomerMemoCommandResponse res = service.updateCustomerMemo(user, customerCode, memoCode, req);

        // then
        then(memo).should(times(1)).changeContent(eq("changed"), any(LocalDateTime.class));
        assertThat(res.getCustomerMemoCode()).isEqualTo(memoCode);
    }

    @Test
    @DisplayName("delete: memo 없으면 CUSTOMER_MEMO_NOT_FOUND")
    void delete_memo_not_found_thenThrow() {
        // given
        CustomUser user = mockUser(1L);
        Long customerCode = 100L;
        Long memoCode = 1L;

        given(customerRepository.findByCustomerCodeAndHotelGroupCode(customerCode, 1L))
                .willReturn(Optional.of(mock(Customer.class)));
        given(customerMemoRepository.findByCustomerMemoCodeAndCustomerCode(memoCode, customerCode))
                .willReturn(Optional.empty());

        // when
        CustomException ex = catchThrowableOfType(
                () -> service.deleteCustomerMemo(user, customerCode, memoCode),
                CustomException.class
        );

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.CUSTOMER_MEMO_NOT_FOUND);
    }

    @Test
    @DisplayName("delete: 이미 deleted면 멱등 처리(return) - employee 조회/삭제호출 안 함")
    void delete_idempotent_when_already_deleted() {
        // given
        CustomUser user = mockUser(1L);
        Long customerCode = 100L;
        Long memoCode = 1L;

        given(customerRepository.findByCustomerCodeAndHotelGroupCode(customerCode, 1L))
                .willReturn(Optional.of(mock(Customer.class)));

        CustomerMemo memo = mock(CustomerMemo.class);
        given(customerMemoRepository.findByCustomerMemoCodeAndCustomerCode(memoCode, customerCode))
                .willReturn(Optional.of(memo));
        given(memo.isDeleted()).willReturn(true);

        // when
        service.deleteCustomerMemo(user, customerCode, memoCode);

        // then
        then(employeeRepository).shouldHaveNoInteractions();
        then(memo).should(never()).delete(anyLong(), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("delete: 정상 삭제면 employeeCode 조회 후 memo.delete 호출")
    void delete_success_calls_delete() {
        // given
        CustomUser user = mockUser(1L);
        stubUsername(user, "hong0");

        Long customerCode = 100L;
        Long memoCode = 1L;

        given(customerRepository.findByCustomerCodeAndHotelGroupCode(customerCode, 1L))
                .willReturn(Optional.of(mock(Customer.class)));

        CustomerMemo memo = mock(CustomerMemo.class);
        given(customerMemoRepository.findByCustomerMemoCodeAndCustomerCode(memoCode, customerCode))
                .willReturn(Optional.of(memo));
        given(memo.isDeleted()).willReturn(false);

        Employee emp = mock(Employee.class);
        given(employeeRepository.findByLoginId("hong0")).willReturn(Optional.of(emp));
        given(emp.getEmployeeCode()).willReturn(10L);

        // when
        service.deleteCustomerMemo(user, customerCode, memoCode);

        // then
        ArgumentCaptor<Long> empCodeCap = ArgumentCaptor.forClass(Long.class);
        then(memo).should(times(1)).delete(empCodeCap.capture(), any(LocalDateTime.class));
        assertThat(empCodeCap.getValue()).isEqualTo(10L);
    }
}
