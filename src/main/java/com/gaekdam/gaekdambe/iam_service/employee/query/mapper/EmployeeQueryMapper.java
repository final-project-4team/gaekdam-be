package com.gaekdam.gaekdambe.iam_service.employee.query.mapper;

import com.gaekdam.gaekdambe.iam_service.employee.command.domain.EmployeeStatus;
import com.gaekdam.gaekdambe.iam_service.employee.query.dto.response.EmployeeQueryEncResponse;
import com.gaekdam.gaekdambe.iam_service.employee.query.dto.response.EmployeeQueryListEncResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EmployeeQueryMapper {

        EmployeeQueryEncResponse findByEmployeeCode(@Param("employeeCode") Long employeeCode);

        // 통합된 동적 검색 메서드 (페이징 지원)
        List<EmployeeQueryListEncResponse> searchEmployees(
                        @Param("nameHash") byte[] nameHash,
                        @Param("phoneHash") byte[] phoneHash,
                        @Param("emailHash") byte[] emailHash,
                        @Param("departmentName") String departmentName,
                        @Param("hotelPositionName") String hotelPositionName,
                        @Param("employeeStatus") EmployeeStatus employeeStatus,
                        @Param("offset") long offset,
                        @Param("limit") int limit);

        // 검색 결과 전체 개수 조회
        long countSearchEmployees(
                        @Param("nameHash") byte[] nameHash,
                        @Param("phoneHash") byte[] phoneHash,
                        @Param("emailHash") byte[] emailHash,
                        @Param("departmentName") String departmentName,
                        @Param("hotelPositionName") String hotelPositionName,
                        @Param("employeeStatus") EmployeeStatus employeeStatus);

}
