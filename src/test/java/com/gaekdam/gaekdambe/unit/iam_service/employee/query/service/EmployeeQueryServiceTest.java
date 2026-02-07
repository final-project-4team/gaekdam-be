package com.gaekdam.gaekdambe.unit.iam_service.employee.query.service;

import com.gaekdam.gaekdambe.global.crypto.DecryptionService;
import com.gaekdam.gaekdambe.global.crypto.SearchHashService;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.iam_service.employee.command.domain.EmployeeStatus;
import com.gaekdam.gaekdambe.iam_service.employee.query.dto.request.EmployeeQuerySearchRequest;
import com.gaekdam.gaekdambe.iam_service.employee.query.dto.response.EmployeeDetailResponse;
import com.gaekdam.gaekdambe.iam_service.employee.query.dto.response.EmployeeListResponse;
import com.gaekdam.gaekdambe.iam_service.employee.query.dto.response.EmployeeQueryEncResponse;
import com.gaekdam.gaekdambe.iam_service.employee.query.dto.response.EmployeeQueryListEncResponse;
import com.gaekdam.gaekdambe.iam_service.employee.query.mapper.EmployeeQueryMapper;
import com.gaekdam.gaekdambe.iam_service.employee.query.service.EmployeeQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmployeeQueryServiceTest {

        @InjectMocks
        private EmployeeQueryService service;

        @Mock
        private EmployeeQueryMapper employeeQueryMapper;
        @Mock
        private DecryptionService decryptionService;
        @Mock
        private SearchHashService searchHashService;

        @Test
        @DisplayName("getEmployeeDetail: 정상 조회 및 복호화")
        void getEmployeeDetail_success() {
                // given
                Long hgCode = 100L;
                Long empCode = 10L;

                // Mock Enc Response
                // fields: code, number, loginId, nameEnc, phoneEnc, emailEnc, dekEnc,
                // deptName, posName, propName, hgName, permName,
                // hiredAt, status(String), created, updated,
                // deptCode, posCode, propCode, hgCode, permCode, failedCnt, lastLogin
                EmployeeQueryEncResponse encResp = new EmployeeQueryEncResponse(
                                empCode, 12345L, "testId",
                                new byte[] { 1 }, new byte[] { 2 }, new byte[] { 3 }, new byte[] { 9 },
                                "Dept", "Pos", "Prop", "HG", "Perm",
                                LocalDateTime.now(), "ACTIVE",
                                LocalDateTime.now(), LocalDateTime.now(),
                                20L, 30L, 40L, hgCode, 50L,
                                0, LocalDateTime.now());

                given(employeeQueryMapper.findByEmployeeCode(empCode)).willReturn(encResp);

                // Mock Decryption
                given(decryptionService.decrypt(eq(empCode), eq(new byte[] { 9 }), eq(new byte[] { 1 })))
                                .willReturn("DecryptedName");
                given(decryptionService.decrypt(eq(empCode), eq(new byte[] { 9 }), eq(new byte[] { 2 })))
                                .willReturn("010-1234-5678");
                given(decryptionService.decrypt(eq(empCode), eq(new byte[] { 9 }), eq(new byte[] { 3 })))
                                .willReturn("test@email.com");

                // when
                EmployeeDetailResponse result = service.getEmployeeDetail(hgCode, empCode, "reason");

                // then
                assertThat(result.employeeCode()).isEqualTo(empCode);
                assertThat(result.employeeName()).isEqualTo("DecryptedName");
                assertThat(result.phoneNumber()).isEqualTo("010-1234-5678");

                verify(employeeQueryMapper).findByEmployeeCode(empCode);
        }

        @Test
        @DisplayName("getEmployeeDetail: 다른 호텔 그룹이면 예외")
        void getEmployeeDetail_diffHotelGroup() {
                // given
                Long myHgCode = 100L;
                Long otherHgCode = 200L;
                Long empCode = 10L;

                EmployeeQueryEncResponse encResp = new EmployeeQueryEncResponse(
                                empCode, 1L, "id", null, null, null, null,
                                null, null, null, null, null,
                                null, null, null, null,
                                null, null, null, otherHgCode, null,
                                0, null);

                given(employeeQueryMapper.findByEmployeeCode(empCode)).willReturn(encResp);

                // when
                Throwable t = catchThrowable(() -> service.getEmployeeDetail(myHgCode, empCode, "r"));

                // then
                assertThat(t).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("searchEmployees: 검색 조건 해싱 및 결과 마스킹")
        void searchEmployees_success() {
                // given
                Long hgCode = 100L;
                EmployeeQuerySearchRequest req = new EmployeeQuerySearchRequest(
                                "searchName", null, null, null, null, null, null,null);
                PageRequest page = new PageRequest();
                page.setPage(1);
                page.setSize(10);
                SortRequest sort = new SortRequest();

                given(searchHashService.nameHash("searchName")).willReturn(new byte[] { 5 });

                // fields: code, number, nameEnc, phoneEnc, emailEnc, loginId, status(Enum),
                // permName, dekEnc
                EmployeeQueryListEncResponse listResp = new EmployeeQueryListEncResponse(
                                10L, 123L,
                                new byte[] { 1 }, new byte[] { 2 }, new byte[] { 3 },
                                "loginId", EmployeeStatus.ACTIVE,
                                "Perm",
                                new byte[] { 9 });

                given(employeeQueryMapper.countSearchEmployees(eq(hgCode), any(), any(), any(), any())).willReturn(1L);
                given(employeeQueryMapper.searchEmployees(eq(hgCode), any(), any(), any(), any(), any(), any()))
                                .willReturn(List.of(listResp));

                // Mock Decryption for list item (to allow masking)
                given(decryptionService.decrypt(eq(10L), eq(new byte[] { 9 }), eq(new byte[] { 1 })))
                                .willReturn("Full Name");
                given(decryptionService.decrypt(eq(10L), eq(new byte[] { 9 }), eq(new byte[] { 2 })))
                                .willReturn("010-1234-5678"); // 11 length
                given(decryptionService.decrypt(eq(10L), eq(new byte[] { 9 }), eq(new byte[] { 3 })))
                                .willReturn("email@test.com");

                // when
                PageResponse<EmployeeListResponse> response = service.searchEmployees(hgCode, req, page, sort);

                // then
                assertThat(response.getTotalElements()).isEqualTo(1L);
                EmployeeListResponse item = response.getContent().get(0);

                // Check Masking
                assertThat(item.employeeName()).isNotNull();
                assertThat(item.phoneNumber()).isNotNull(); // "010-****-5678"

                verify(searchHashService).nameHash("searchName");
                verify(employeeQueryMapper).searchEmployees(eq(hgCode), eq(new byte[] { 5 }), any(), any(), any(),
                                any(),
                                any());
        }

        @Test
        @DisplayName("getMyPage: 정상 조회 성공")
        void getMyPage_success() {
                // given
                Long hgCode = 100L;
                String loginId = "testUser";

                EmployeeQueryEncResponse encResp = new EmployeeQueryEncResponse(
                                10L, 12345L, loginId,
                                new byte[] { 1 }, new byte[] { 2 }, new byte[] { 3 }, new byte[] { 9 },
                                "Dept", "Pos", "Prop", "HG", "Perm",
                                LocalDateTime.now(), "ACTIVE",
                                LocalDateTime.now(), LocalDateTime.now(),
                                20L, 30L, 40L, hgCode, 50L,
                                0, LocalDateTime.now());

                given(employeeQueryMapper.findMyPage(hgCode, loginId)).willReturn(encResp);
                given(decryptionService.decrypt(eq(10L), eq(new byte[] { 9 }), eq(new byte[] { 1 })))
                                .willReturn("DecryptedName");
                given(decryptionService.decrypt(eq(10L), eq(new byte[] { 9 }), eq(new byte[] { 2 })))
                                .willReturn("010-1234-5678");
                given(decryptionService.decrypt(eq(10L), eq(new byte[] { 9 }), eq(new byte[] { 3 })))
                                .willReturn("test@email.com");

                // when
                EmployeeDetailResponse result = service.getMyPage(hgCode, loginId);

                // then
                assertThat(result.employeeCode()).isEqualTo(10L);
                assertThat(result.loginId()).isEqualTo(loginId);
                assertThat(result.employeeName()).isEqualTo("DecryptedName");
                verify(employeeQueryMapper).findMyPage(hgCode, loginId);
        }

        @Test
        @DisplayName("getMyPage: response가 null이면 예외 발생")
        void getMyPage_fail_nullResponse() {
                // given
                Long hgCode = 100L;
                String loginId = "notExistUser";

                given(employeeQueryMapper.findMyPage(hgCode, loginId)).willReturn(null);

                // when
                Throwable t = catchThrowable(() -> service.getMyPage(hgCode, loginId));

                // then
                assertThat(t).isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Not found");
        }

        @Test
        @DisplayName("getMyPage: hotelGroupCode 불일치 시 예외 발생")
        void getMyPage_fail_hotelGroupMismatch() {
                // given
                Long myHgCode = 100L;
                Long otherHgCode = 200L;
                String loginId = "testUser";

                EmployeeQueryEncResponse encResp = new EmployeeQueryEncResponse(
                                10L, 12345L, loginId,
                                null, null, null, null,
                                null, null, null, null, null,
                                null, null, null, null,
                                null, null, null, otherHgCode, null,
                                0, null);

                given(employeeQueryMapper.findMyPage(myHgCode, loginId)).willReturn(encResp);

                // when
                Throwable t = catchThrowable(() -> service.getMyPage(myHgCode, loginId));

                // then
                assertThat(t).isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Not match hotelGroupCode");
        }
}
