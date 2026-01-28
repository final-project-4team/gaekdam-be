package com.gaekdam.gaekdambe.hotel_service.department.query.service;

import com.gaekdam.gaekdambe.hotel_service.department.query.dto.response.DepartmentListResponse;
import com.gaekdam.gaekdambe.hotel_service.department.query.mapper.DepartmentMapper;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DepartmentQueryService {

  private final DepartmentMapper departmentMapper;

  public List<DepartmentListResponse> getDepartmentList(Long hotelGroupCode) {

    List<DepartmentListResponse> list = departmentMapper.findDepartmentList(hotelGroupCode);

    return list;

  }

}
