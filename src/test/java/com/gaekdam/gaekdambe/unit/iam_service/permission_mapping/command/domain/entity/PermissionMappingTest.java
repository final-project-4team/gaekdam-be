package com.gaekdam.gaekdambe.unit.iam_service.permission_mapping.command.domain.entity;

import com.gaekdam.gaekdambe.iam_service.permission.command.domain.entity.Permission;
import com.gaekdam.gaekdambe.iam_service.permission_mapping.command.domain.entity.PermissionMapping;
import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.entity.PermissionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class PermissionMappingTest {

    @Test
    @DisplayName("createPermissionMapping: 권한 매핑 생성 성공")
    void createPermissionMapping_success() {
        // given
        Permission permission = mock(Permission.class);
        PermissionType permissionType = mock(PermissionType.class);

        // when
        PermissionMapping mapping = PermissionMapping.createPermissionMapping(permission, permissionType);

        // then
        assertThat(mapping.getPermission()).isEqualTo(permission);
        assertThat(mapping.getPermissionType()).isEqualTo(permissionType);
    }
}
