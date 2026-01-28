package com.gaekdam.gaekdambe.iam_service.log.query.mapper;

import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.iam_service.log.query.dto.request.PermissionChangedLogSearchRequest;
import com.gaekdam.gaekdambe.iam_service.log.query.dto.response.PermissionChangedLogQueryResponse;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PermissionChangedLogMapper {

        List<PermissionChangedLogQueryResponse> findPermissionChangedLogs(
                        @Param("hotelGroupCode") Long hotelGroupCode,
                        @Param("page") PageRequest page,
                        @Param("search") PermissionChangedLogSearchRequest search,
                        @Param("sort") SortRequest sort);

        long countPermissionChangedLogs(
                        @Param("hotelGroupCode") Long hotelGroupCode,
                        @Param("search") PermissionChangedLogSearchRequest search);
}
