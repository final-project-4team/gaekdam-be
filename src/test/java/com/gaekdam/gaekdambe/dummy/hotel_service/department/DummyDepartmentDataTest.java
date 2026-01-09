package com.gaekdam.gaekdambe.dummy.hotel_service.department;

import com.gaekdam.gaekdambe.hotel_service.department.command.domain.entity.Department;
import com.gaekdam.gaekdambe.hotel_service.department.command.infrastructure.DepartmentRepository;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.infrastructure.repository.HotelGroupRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@Rollback(false)
public class DummyDepartmentDataTest {

  @Autowired
  DepartmentRepository departmentRepository;
  @Autowired
  HotelGroupRepository hotelGroupRepository;

  @Test
  @DisplayName("부서 생성")
  void createDepartment() {
    Object[][] departmentsDummy = {
        {"경영", 1L},
        {"지원", 1L},
        {"객실", 1L},
        {"식음", 1L},
        {"조리", 1L},
        {"세일즈", 1L},
        {"홍보", 1L},
        {"시설", 1L},
    };

    for (Object[] departmentDummy : departmentsDummy) {
      Department department = Department.createDepartment(
          (String)departmentDummy[0],
          hotelGroupRepository.findById((long)departmentDummy[1]).orElseThrow()
      );
      departmentRepository.save(department);
    }
  }

}
