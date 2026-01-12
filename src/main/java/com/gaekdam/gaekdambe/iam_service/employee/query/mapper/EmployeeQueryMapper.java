package com.gaekdam.gaekdambe.iam_service.employee.query.mapper;

import com.gaekdam.gaekdambe.iam_service.employee.query.dto.response.EmployeeQueryEncResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EmployeeQueryMapper {

    List<EmployeeQueryEncResponse> findAllEmployees();

    EmployeeQueryEncResponse findByEmployeeCode(@Param("employeeCode") Long employeeCode);

    // 통합된 동적 검색 메서드 (페이징 지원)
    List<EmployeeQueryEncResponse> searchEmployees(
            @Param("nameHash") byte[] nameHash,
            @Param("phoneHash") byte[] phoneHash,
            @Param("emailHash") byte[] emailHash,
            @Param("offset") long offset,
            @Param("limit") int limit);

    // 검색 결과 전체 개수 조회
    long countSearchEmployees(
            @Param("nameHash") byte[] nameHash,
            @Param("phoneHash") byte[] phoneHash,
            @Param("emailHash") byte[] emailHash);

    // 비밀번호로 조회 (인증용)
    List<EmployeeQueryEncResponse> findByPassword(@Param("password") String password);
}
