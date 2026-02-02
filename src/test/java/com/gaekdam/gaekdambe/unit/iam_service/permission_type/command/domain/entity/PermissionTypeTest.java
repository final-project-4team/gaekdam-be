package com.gaekdam.gaekdambe.unit.iam_service.permission_type.command.domain.entity;

import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.entity.PermissionType;
import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.seeds.PermissionTypeKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PermissionTypeTest {

    @Test
    @DisplayName("createPermissionType: 권한 타입 생성 성공")
    void createPermissionType_success() {
        // given
        PermissionTypeKey key = PermissionTypeKey.EMPLOYEE_CREATE;
        String name = "직원 생성";
        String resource = "EMPLOYEE";
        String action = "CREATE";

        // when
        PermissionType type = PermissionType.createPermissionType(key, name, resource, action);

        // then
        assertThat(type.getPermissionTypeKey()).isEqualTo(key);
        assertThat(type.getPermissionTypeName()).isEqualTo(name);
        assertThat(type.getPermissionTypeResource()).isEqualTo(resource);
        assertThat(type.getPermissionTypeAction()).isEqualTo(action);
    }

    @Test
    @DisplayName("createPermissionType: 필수값 누락 시 예외 발생")
    void createPermissionType_fail() {
        // given & when & then
        assertThatThrownBy(() -> PermissionType.createPermissionType(null, "Name", "Res", "Act"))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(
                () -> PermissionType.createPermissionType(PermissionTypeKey.EMPLOYEE_CREATE, null, "Res", "Act"))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(
                () -> PermissionType.createPermissionType(PermissionTypeKey.EMPLOYEE_CREATE, "Name", null, "Act"))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(
                () -> PermissionType.createPermissionType(PermissionTypeKey.EMPLOYEE_CREATE, "Name", "Res", null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
