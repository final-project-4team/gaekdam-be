package com.gaekdam.gaekdambe.unit.iam_service.permission.command.application.service;

import com.gaekdam.gaekdambe.global.exception.CustomException;
import com.gaekdam.gaekdambe.global.exception.ErrorCode;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.domain.entity.HotelGroup;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.infrastructure.repository.HotelGroupRepository;
import com.gaekdam.gaekdambe.iam_service.employee.command.domain.entity.Employee;
import com.gaekdam.gaekdambe.iam_service.employee.command.infrastructure.EmployeeRepository;
import com.gaekdam.gaekdambe.iam_service.log.command.application.service.AuditLogService;
import com.gaekdam.gaekdambe.iam_service.permission.command.application.dto.request.PermissionCreateRequest;
import com.gaekdam.gaekdambe.iam_service.permission.command.application.dto.request.PermissionUpdateRequest;
import com.gaekdam.gaekdambe.iam_service.permission.command.application.service.PermissionCommandService;
import com.gaekdam.gaekdambe.iam_service.permission.command.domain.entity.Permission;
import com.gaekdam.gaekdambe.iam_service.permission.command.infrastructure.PermissionRepository;
import com.gaekdam.gaekdambe.iam_service.permission_mapping.command.domain.entity.PermissionMapping;
import com.gaekdam.gaekdambe.iam_service.permission_mapping.command.infrastructure.PermissionMappingRepository;
import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.entity.PermissionType;
import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.seeds.PermissionTypeKey;
import com.gaekdam.gaekdambe.iam_service.permission_type.command.infrastructure.PermissionTypeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermissionCommandServiceTest {

    @InjectMocks
    private PermissionCommandService service;

    @Mock
    private HotelGroupRepository hotelGroupRepository;
    @Mock
    private PermissionTypeRepository permissionTypeRepository;
    @Mock
    private PermissionMappingRepository permissionMappingRepository;
    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private AuditLogService auditLogService;

    @Test
    @DisplayName("createPermission: 권한 생성 성공")
    void createPermission_success() {
        // given
        Long hgCode = 1L;
        PermissionCreateRequest req = new PermissionCreateRequest("newPerm", List.of(1L, 2L));
        HotelGroup hg = mock(HotelGroup.class);
        Permission permission = mock(Permission.class);
        PermissionType pt1 = mock(PermissionType.class);
        PermissionType pt2 = mock(PermissionType.class);

        given(hotelGroupRepository.findById(hgCode)).willReturn(Optional.of(hg));
        given(permissionRepository.save(any(Permission.class))).willReturn(permission);
        given(permissionTypeRepository.findAllById(req.permissionTypeList())).willReturn(List.of(pt1, pt2));

        // when
        String result = service.createPermission(req, hgCode);

        // then
        assertThat(result).isEqualTo("권한 생성 완료");
        verify(permissionRepository).save(any(Permission.class));
        verify(permissionMappingRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("updatePermission: 권한 수정 성공 및 AuditLog 검증")
    void updatePermission_success() {
        // given
        Long permCode = 10L;
        PermissionUpdateRequest req = new PermissionUpdateRequest(List.of(3L)); // New type
        Long hgCode = 1L;
        String accessorId = "admin";

        Permission permission = mock(Permission.class);
        HotelGroup hg = mock(HotelGroup.class);
        Employee accessor = mock(Employee.class);
        PermissionType oldPt = mock(PermissionType.class);
        PermissionMapping oldMap = mock(PermissionMapping.class);
        PermissionType newPt = mock(PermissionType.class);

        given(permissionRepository.findById(permCode)).willReturn(Optional.of(permission));
        given(permission.getHotelGroup()).willReturn(hg);
        given(hg.getHotelGroupCode()).willReturn(hgCode);
        given(employeeRepository.findByLoginId(accessorId)).willReturn(Optional.of(accessor));

        // Old state
        given(permission.getPermissionName()).willReturn("oldName");
        given(oldPt.getPermissionTypeKey()).willReturn(PermissionTypeKey.EMPLOYEE_READ);
        given(oldMap.getPermissionType()).willReturn(oldPt);
        given(permissionMappingRepository.findAllByPermission(permission)).willReturn(List.of(oldMap));

        // New state
        given(permissionTypeRepository.findAllById(req.permissionTypeList())).willReturn(List.of(newPt));
        given(newPt.getPermissionTypeKey()).willReturn(PermissionTypeKey.EMPLOYEE_CREATE);

        // when
        String result = service.updatePermission(permCode, req, hgCode, accessorId);

        // then
        assertThat(result).isEqualTo("권한 수정 완료");
        verify(permissionMappingRepository).deleteAllByPermission(permission);
        verify(permissionMappingRepository).saveAll(anyList());
        verify(permissionRepository).save(permission);

        // AuditLog verification (Check if diff details passed)
        verify(auditLogService).saveAuditLog(
                eq(accessor),
                eq(PermissionTypeKey.PERMISSION_UPDATE),
                contains("권한 타입 변경"), // detail check
                contains("EMPLOYEE_READ"), // prev
                contains("EMPLOYEE_CREATE") // new
        );
    }

    @Test
    @DisplayName("deletePermission: 권한 삭제 및 직원 일괄 이동 성공")
    void deletePermission_success() {
        // given
        Long permCode = 10L;
        Long hgCode = 1L;
        Permission permission = mock(Permission.class);
        HotelGroup hg = mock(HotelGroup.class);

        given(permissionRepository.findById(permCode)).willReturn(Optional.of(permission));
        given(permission.getHotelGroup()).willReturn(hg);
        given(hg.getHotelGroupCode()).willReturn(hgCode);

        // Default permission (ID:1) exists
        given(permissionRepository.existsById(1L)).willReturn(true);

        // when
        String result = service.deletePermission(permCode, hgCode);

        // then
        assertThat(result).isEqualTo("권한 삭제");
        // Verify bulk update
        verify(employeeRepository).bulkUpdatePermission(permCode, 1L);

        verify(permissionMappingRepository).deleteAllByPermission(permission);
        verify(permission).deletePermission();
        verify(permissionRepository).save(permission);
    }

    @Test
    @DisplayName("deletePermission: 기본 권한(ID:1) 없으면 실패")
    void deletePermission_fail_noDefault() {
        // given
        Long permCode = 10L;
        Long hgCode = 1L;
        Permission permission = mock(Permission.class);
        HotelGroup hg = mock(HotelGroup.class);

        given(permissionRepository.findById(permCode)).willReturn(Optional.of(permission));
        given(permission.getHotelGroup()).willReturn(hg);
        given(hg.getHotelGroupCode()).willReturn(hgCode);

        given(permissionRepository.existsById(1L)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> service.deletePermission(permCode, hgCode))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("기본 권한(ID:1)이 존재하지 않아");
    }

    @Test
    @DisplayName("deletePermission: 호텔 그룹 불일치 시 예외 발생")
    void deletePermission_fail_hotelGroupMismatch() {
        // given
        Long permCode = 10L;
        Long myHgCode = 100L;
        Long otherHgCode = 200L;

        Permission permission = mock(Permission.class);
        HotelGroup otherHg = mock(HotelGroup.class);

        given(permissionRepository.findById(permCode)).willReturn(Optional.of(permission));
        given(permission.getHotelGroup()).willReturn(otherHg);
        given(otherHg.getHotelGroupCode()).willReturn(otherHgCode);

        // when & then
        assertThatThrownBy(() -> service.deletePermission(permCode, myHgCode))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.HOTEL_GROUP_CODE_NOT_MATCH);
    }

    @Test
    @DisplayName("updatePermission: 호텔 그룹 불일치 시 예외 발생")
    void updatePermission_fail_hotelGroupMismatch() {
        // given
        Long permCode = 10L;
        PermissionUpdateRequest req = new PermissionUpdateRequest(List.of(3L));
        Long myHgCode = 100L;
        Long otherHgCode = 200L;
        String accessorId = "admin";

        Permission permission = mock(Permission.class);
        HotelGroup otherHg = mock(HotelGroup.class);

        given(permissionRepository.findById(permCode)).willReturn(Optional.of(permission));
        given(permission.getHotelGroup()).willReturn(otherHg);
        given(otherHg.getHotelGroupCode()).willReturn(otherHgCode);

        // when & then
        assertThatThrownBy(() -> service.updatePermission(permCode, req, myHgCode, accessorId))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.HOTEL_GROUP_CODE_NOT_MATCH);
    }
}
