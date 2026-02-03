package com.gaekdam.gaekdambe.iam_service.log.query.mapper;

import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.iam_service.log.query.dto.request.LoginLogSearchRequest;
import com.gaekdam.gaekdambe.iam_service.log.query.dto.response.LoginLogQueryResponse;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LoginLogMapper {
  List<LoginLogQueryResponse> findLoginLogs(
      @Param("hotelGroupCode") Long hotelGroupCode,
      @Param("page") PageRequest page,
      @Param("search") LoginLogSearchRequest search,
      @Param("sort") SortRequest sort
  );
  long countLoginLogs(
      @Param("hotelGroupCode") Long hotelGroupCode,
      @Param("search") LoginLogSearchRequest search
  );

}
