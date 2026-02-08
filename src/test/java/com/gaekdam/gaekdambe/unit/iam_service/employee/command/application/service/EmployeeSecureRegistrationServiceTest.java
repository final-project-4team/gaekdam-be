package com.gaekdam.gaekdambe.unit.iam_service.employee.command.application.service;

import com.gaekdam.gaekdambe.global.crypto.AesCryptoUtils;
import com.gaekdam.gaekdambe.global.crypto.DataKey;
import com.gaekdam.gaekdambe.global.crypto.KmsService;
import com.gaekdam.gaekdambe.global.crypto.PasswordValidator;
import com.gaekdam.gaekdambe.global.crypto.SearchHashService;
import com.gaekdam.gaekdambe.global.smtp.MailSendService;
import com.gaekdam.gaekdambe.hotel_service.department.command.domain.entity.Department;
import com.gaekdam.gaekdambe.hotel_service.department.command.infrastructure.DepartmentRepository;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.domain.entity.HotelGroup;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.infrastructure.repository.HotelGroupRepository;
import com.gaekdam.gaekdambe.hotel_service.position.command.domain.entity.HotelPosition;
import com.gaekdam.gaekdambe.hotel_service.position.command.infrastructure.repository.HotelPositionRepository;
import com.gaekdam.gaekdambe.hotel_service.property.command.domain.entity.Property;
import com.gaekdam.gaekdambe.hotel_service.property.command.infrastructure.PropertyRepository;
import com.gaekdam.gaekdambe.iam_service.employee.command.application.dto.request.EmployeeSecureRegistrationRequest;
import com.gaekdam.gaekdambe.iam_service.employee.command.application.service.EmployeeSecureRegistrationService;
import com.gaekdam.gaekdambe.iam_service.employee.command.domain.entity.Employee;
import com.gaekdam.gaekdambe.iam_service.employee.command.infrastructure.EmployeeRepository;
import com.gaekdam.gaekdambe.iam_service.permission.command.domain.entity.Permission;
import com.gaekdam.gaekdambe.iam_service.permission.command.infrastructure.PermissionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class EmployeeSecureRegistrationServiceTest {

    @InjectMocks
    private EmployeeSecureRegistrationService service;

    @Mock private EmployeeRepository employeeRepository;
    @Mock private KmsService kmsService;
    @Mock private SearchHashService searchHashService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private DepartmentRepository departmentRepository;
    @Mock private HotelPositionRepository hotelPositionRepository;
    @Mock private PropertyRepository propertyRepository;
    @Mock private HotelGroupRepository hotelGroupRepository;
    @Mock private PermissionRepository permissionRepository;
    @Mock private MailSendService mailSendService;

    // 🔥🔥🔥 이게 빠져 있었음
    @Mock
    private com.gaekdam.gaekdambe.iam_service.employee.command.infrastructure.BatchEmployeeRepository
            batchEmployeeRepository;

    @Test
    @DisplayName("create : 직원 리스트 일괄 등록 성공")
    void registerEmployees_success() {

        Long hotelGroupCode = 100L;

        EmployeeSecureRegistrationRequest req =
                new EmployeeSecureRegistrationRequest(
                        111L, "user1", "Password123!",
                        "email1@test.com", "010-1111-1111", "Name1",
                        10L, 20L, 30L, 40L
                );

        List<EmployeeSecureRegistrationRequest> requests = List.of(req);

        // HotelGroup
        HotelGroup hotelGroup = Mockito.mock(HotelGroup.class);
        given(hotelGroupRepository.findById(hotelGroupCode))
                .willReturn(Optional.of(hotelGroup));

        // 연관 엔티티
        given(departmentRepository.findById(any()))
                .willReturn(Optional.of(Mockito.mock(Department.class)));
        given(hotelPositionRepository.findById(any()))
                .willReturn(Optional.of(Mockito.mock(HotelPosition.class)));
        given(propertyRepository.findById(any()))
                .willReturn(Optional.of(Mockito.mock(Property.class)));
        given(permissionRepository.findById(any()))
                .willReturn(Optional.of(Mockito.mock(Permission.class)));

        // 암호화/해시
        given(kmsService.generateDataKey())
                .willReturn(new DataKey(new byte[32], new byte[32]));
        given(passwordEncoder.encode(anyString())).willReturn("encoded");
        given(searchHashService.phoneHash(anyString())).willReturn(new byte[]{1});
        given(searchHashService.nameHash(anyString())).willReturn(new byte[]{1});
        given(searchHashService.emailHash(anyString())).willReturn(new byte[]{1});

        try (
                MockedStatic<PasswordValidator> pwd =
                        Mockito.mockStatic(PasswordValidator.class, Mockito.withSettings().lenient());
                MockedStatic<AesCryptoUtils> aes =
                        Mockito.mockStatic(AesCryptoUtils.class, Mockito.withSettings().lenient())
        ) {
            aes.when(() -> AesCryptoUtils.encrypt(anyString(), any()))
                    .thenReturn(new byte[]{1});

            // when
            service.registerEmployees(hotelGroupCode, requests);

            // then
            verify(batchEmployeeRepository).saveAllBatch(any());
        }
    }
}
