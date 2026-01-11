package com.gaekdam.gaekdambe.iam_service.employee.query.mapper;

import com.gaekdam.gaekdambe.iam_service.employee.query.dto.response.EmployeeDetailResponse;
import com.gaekdam.gaekdambe.iam_service.employee.query.dto.response.EmployeeListResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EmployeeQueryMapper {

    List<EmployeeListResponse> findAllEmployees();

    EmployeeDetailResponse findByEmployeeCode(@Param("employeeCode") Long employeeCode);

    List<EmployeeListResponse> findByEmailHash(@Param("emailHash") byte[] emailHash);

    List<EmployeeListResponse> findByPhoneHash(@Param("phoneHash") byte[] phoneHash);

    List<EmployeeListResponse> findByNameHash(@Param("nameHash") byte[] nameHash);

    // 비밀번호(해시/평문)로 조회
    List<EmployeeListResponse> findByPassword(@Param("password") String password);

    // 이름과 전화번호 복합 조회
    List<EmployeeListResponse> findByNameAndPhoneHash(@Param("nameHash") byte[] nameHash,
            @Param("phoneHash") byte[] phoneHash);
}
