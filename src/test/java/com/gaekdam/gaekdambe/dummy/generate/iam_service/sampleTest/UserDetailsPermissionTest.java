/*
package com.gaekdam.gaekdambe.dummy.generate.iam_service.sampleTest;

import com.gaekdam.gaekdambe.global.config.security.CustomUserDetailsService;
import com.gaekdam.gaekdambe.iam_service.employee.command.domain.entity.Employee;
import com.gaekdam.gaekdambe.iam_service.employee.command.infrastructure.EmployeeRepository;
import com.gaekdam.gaekdambe.iam_service.permission.command.domain.entity.Permission;
import com.gaekdam.gaekdambe.iam_service.permission.command.infrastructure.PermissionRepository;
import com.gaekdam.gaekdambe.iam_service.permission_mapping.command.domain.entity.PermissionMapping;
import com.gaekdam.gaekdambe.iam_service.permission_mapping.command.infrastructure.PermissionMappingRepository;
import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.entity.PermissionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserDetailsPermissionTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private PermissionMappingRepository permissionMappingRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("LoadUserByUsername should load both Role and Granular Permissions")
    void testLoadUserPermissions() {
        // Given
        String loginId = "testUser";
        Long roleCode = 100L;

        // Mock Employee
        Employee mockEmployee = Employee.createEmployee(
                12345L, loginId, "hash", new byte[0], new byte[0], new byte[0],
                new byte[0], new byte[0], new byte[0], new byte[0],
                LocalDateTime.now(), 1L, 1L, 1L, 1L, roleCode);

        // Mock Permission (Role)
        Permission mockPermission = Permission.createPermission("MANAGER", 1L);
        // Set ID using reflection if necessary, but here permissionCode match isn't
        // strictly checked by logic unless we mock finding it

        // Mock PermissionTypes
        PermissionType pt1 = PermissionType.createPermissionType("USER_READ", "User Read", "user", "read", 1L);
        PermissionType pt2 = PermissionType.createPermissionType("POST_WRITE", "Post Write", "post", "write", 1L);

        // Mock PermissionMappings
        PermissionMapping pm1 = new PermissionMapping();
        ReflectionTestUtils.setField(pm1, "permissionType", pt1);
        PermissionMapping pm2 = new PermissionMapping();
        ReflectionTestUtils.setField(pm2, "permissionType", pt2);

        // Define Mock Behavior
        given(employeeRepository.findByLoginId(loginId)).willReturn(Optional.of(mockEmployee));
        given(permissionRepository.findById(roleCode)).willReturn(Optional.of(mockPermission));
        given(permissionMappingRepository.findAllByPermission(mockPermission)).willReturn(List.of(pm1, pm2));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(loginId);

        // Then
        assertThat(userDetails.getUsername()).isEqualTo(loginId);

        // Check Authorities
        // Should have: ROLE_MANAGER, USER_READ, POST_WRITE
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactlyInAnyOrder("ROLE_MANAGER", "USER_READ", "POST_WRITE");
    }
}
*/
