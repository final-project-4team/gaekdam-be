package com.gaekdam.gaekdambe.unit.iam_service.permission.query.service;

import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.iam_service.permission.query.dto.request.PermissionQueryRequest;
import com.gaekdam.gaekdambe.iam_service.permission.query.dto.request.PermissionSearchRequest;
import com.gaekdam.gaekdambe.iam_service.permission.query.dto.response.PermissionListResponse;
import com.gaekdam.gaekdambe.iam_service.permission.query.dto.response.PermissionNameListResponse;
import com.gaekdam.gaekdambe.iam_service.permission.query.mapper.PermissionMapper;
import com.gaekdam.gaekdambe.iam_service.permission.query.service.PermissionQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PermissionQueryServiceTest {

    @InjectMocks
    private PermissionQueryService service;

    @Mock
    private PermissionMapper permissionMapper;

    @Test
    @DisplayName("getPermissionList: 페이징 목록 조회 성공")
    void getPermissionList_success() {
        // given
        Long hgCode = 1L;
        PermissionQueryRequest req = new PermissionQueryRequest(1, 10, "name", List.of("res"), "id", "ASC");
        PermissionListResponse item = new PermissionListResponse();
        item.setPermissionCode(1L);
        item.setPermissionName("Perm1");

        given(permissionMapper.findPermissionList(any(PageRequest.class), any(), any(PermissionSearchRequest.class),
                eq(hgCode), any(SortRequest.class)))
                .willReturn(List.of(item));
        given(permissionMapper.countPermissionList(any(PermissionSearchRequest.class))).willReturn(1L);

        // when
        PageResponse<PermissionListResponse> response = service.getPermissionList(req, hgCode);

        // then
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getTotalElements()).isEqualTo(1L);
        assertThat(response.getContent().get(0).getPermissionName()).isEqualTo("Perm1");
    }

    @Test
    @DisplayName("getPermissionNameList: 권한 이름 목록 조회 성공")
    void getPermissionNameList_success() {
        // given
        Long hgCode = 1L;
        PermissionNameListResponse item = new PermissionNameListResponse(10L, "Admin");

        given(permissionMapper.findPermissionNameList(hgCode)).willReturn(List.of(item));

        // when
        List<PermissionNameListResponse> list = service.getPermissionNameList(hgCode);

        // then
        assertThat(list).hasSize(1);
        assertThat(list.get(0).permissionName()).isEqualTo("Admin");
    }
}
