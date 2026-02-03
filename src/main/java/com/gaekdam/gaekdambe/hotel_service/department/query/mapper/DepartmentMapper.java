package com.gaekdam.gaekdambe.hotel_service.department.query.mapper;

import com.gaekdam.gaekdambe.hotel_service.department.query.dto.response.DepartmentListResponse;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DepartmentMapper {

  List<DepartmentListResponse> findDepartmentList(@Param("hotelGroupCode")Long hotelGroupCode);

}
