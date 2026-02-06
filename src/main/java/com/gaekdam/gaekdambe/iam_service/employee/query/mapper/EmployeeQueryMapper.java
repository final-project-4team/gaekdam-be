package com.gaekdam.gaekdambe.iam_service.employee.query.mapper;

import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.iam_service.employee.query.dto.request.EmployeeQuerySearchRequest;
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
      @Param("hotelGroupCode") Long hotelGroupCode,
      @Param("nameHash") byte[] nameHash,
      @Param("phoneHash") byte[] phoneHash,
      @Param("emailHash") byte[] emailHash,
      @Param("search") EmployeeQuerySearchRequest search,
      @Param("page") PageRequest page,
      @Param("sort") SortRequest sort
  );

  // 검색 결과 전체 개수 조회
  long countSearchEmployees(
      @Param("hotelGroupCode") Long hotelGroupCode,
      @Param("nameHash") byte[] nameHash,
      @Param("phoneHash") byte[] phoneHash,
      @Param("emailHash") byte[] emailHash,
      @Param("search") EmployeeQuerySearchRequest search
  );

  EmployeeQueryEncResponse findMyPage(@Param("hotelGroupCode") Long hotelGroupCode,@Param("loginId") String LoginId);

}
