/*
package com.gaekdam.gaekdambe.dummy.generate.iam_service.sampleTest;

import com.gaekdam.gaekdambe.iam_service.employee.command.application.dto.request.EmployeeSecureRegistrationRequest;
import com.gaekdam.gaekdambe.iam_service.employee.query.dto.response.EmployeeDetailResponse;
import com.gaekdam.gaekdambe.iam_service.employee.query.dto.response.EmployeeListResponse;
import com.gaekdam.gaekdambe.iam_service.employee.query.service.EmployeeQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class EmployeeSearchTest {

        @Autowired
        private EmployeeQueryService queryService;

        @Autowired
        private com.gaekdam.gaekdambe.iam_service.employee.command.application.service.EmployeeSecureRegistrationService registrationService;

        private Long savedHongCode;

        @BeforeEach
        void setUp() {
                savedHongCode = registrationService.registerEmployee(
                                new EmployeeSecureRegistrationRequest(
                                                20001L, "search.hong", "pass123",
                                                "search.hong@company.com", "010-9999-9999", "홍길동",
                                                1L, 1L, 1L, 1L, 1L));

                registrationService.registerEmployee(
                                new EmployeeSecureRegistrationRequest(
                                                20002L, "search.chulsoo", "password123",
                                                "search.chulsoo@company.com", "010-8888-8888", "김철수",
                                                1L, 1L, 1L, 1L, 1L));
        }

        @Test
        @DisplayName("리스트 반환 및 마스킹 적용 검색 시나리오 테스트")
        void testEmployeeSearchScenarios() {
                System.out.println("\n=== [시작] 직원 검색 시나리오 테스트 (List 반환 & 마스킹) ===");

                // 전체 리스트 조회
                List<EmployeeListResponse> allList = queryService.getEmployeeList();
                System.out.println(" 전체 리스트 조회 ");
                allList.forEach(e -> System.out.println(" - " + e.nameMasked() + " (ID: " + e.loginId() + ", Phone: "
                                + e.phoneMasked() + ", Email: " + e.emailMasked() + ", Dept: " + e.departmentName()
                                + ", Pos: " + e.hotelPositionName() + ", Status: " + e.employeeStatus() + ")"));
                assertThat(allList).hasSizeGreaterThanOrEqualTo(2);

                // 이메일로 조회 (결과: List)
                List<EmployeeListResponse> byEmail = queryService.searchEmployees(null, null,
                                "search.hong@company.com", PageRequest.of(0, 10)).getContent();
                System.out.println(" 이메일 검색 ");
                byEmail.forEach(e -> System.out.println(" - " + e.nameMasked() + " (" + e.emailMasked() + ")"));
                assertThat(byEmail).isNotEmpty();
                assertThat(byEmail.get(0).nameMasked()).contains("*"); // 마스킹 확인

                // 이름으로 조회 (결과: List)
                List<EmployeeListResponse> byName = queryService
                                .searchEmployees("홍길동", null, null, PageRequest.of(0, 10)).getContent();
                System.out.println("이름 검색 ");
                byName.forEach(e -> System.out.println(" - " + e.nameMasked() + " (ID: " + e.loginId() + ")"));
                assertThat(byName).isNotEmpty();

                // 비밀번호로 조회 (결과: List)
                List<EmployeeListResponse> byPassword = queryService.findByPassword("pass123");
                System.out.println("4. 비밀번호('pass123') 검색 결과:");
                byPassword.forEach(e -> System.out.println(" - " + e.nameMasked() + " (Login: " + e.loginId() + ")"));
                assertThat(byPassword).isNotEmpty();

                // 이름과 전화번호 둘다 일치 조회 (결과: List)
                List<EmployeeListResponse> byNameAndPhone = queryService
                                .searchEmployees("홍길동", "010-9999-9999", null, PageRequest.of(0, 10)).getContent();
                System.out.println("이름 전화번호 검색 ");
                byNameAndPhone.forEach(e -> System.out.println(" - " + e.nameMasked() + " (" + e.phoneMasked() + ")"));
                assertThat(byNameAndPhone).isNotEmpty();

                // 코드를 통한 상세조회 (마스킹 안된값)
                EmployeeDetailResponse detail = queryService.getEmployeeDetail(savedHongCode);
                System.out.println("직원 코드" + savedHongCode + " 상세 조회");
                System.out.println(" - 이름: " + detail.name());
                System.out.println(" - 전화번호: " + detail.phone());
                System.out.println(" - 이메일: " + detail.email());
                assertThat(detail.name()).isEqualTo("홍길동");
                assertThat(detail.phone()).isEqualTo("010-9999-9999");
                assertThat(detail.email()).isEqualTo("search.hong@company.com");

                System.out.println("=== [종료] 직원 검색 시나리오 테스트 완료 ===\n");
        }
}
*/
