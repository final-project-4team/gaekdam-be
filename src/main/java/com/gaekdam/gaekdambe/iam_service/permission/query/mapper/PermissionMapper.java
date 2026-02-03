package com.gaekdam.gaekdambe.iam_service.permission.query.mapper;

import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.iam_service.permission.query.dto.request.PermissionSearchRequest;
import com.gaekdam.gaekdambe.iam_service.permission.query.dto.response.PermissionListResponse;
import com.gaekdam.gaekdambe.iam_service.permission.query.dto.response.PermissionNameListResponse;
import java.util.List;
import org.apache.ibatis.annotations.Param;


public interface PermissionMapper {

  List<PermissionListResponse> findPermissionList(
      @Param("page") PageRequest page,
      @Param("offset")  Integer offset,
      @Param("search") PermissionSearchRequest search,
      @Param("hotelGroupCode")Long hotelGroupCode,
      @Param("sort") SortRequest sort);

  long countPermissionList(
      @Param("search") PermissionSearchRequest search);

  List<PermissionNameListResponse> findPermissionNameList(@Param("hotelGroupCode")Long hotelGroupCode);
}
