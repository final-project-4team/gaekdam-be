package com.gaekdam.gaekdambe.dummy.generate.hotel_service.department;


import com.gaekdam.gaekdambe.hotel_service.department.command.domain.entity.Department;
import com.gaekdam.gaekdambe.hotel_service.department.command.infrastructure.DepartmentRepository;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.infrastructure.repository.HotelGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DummyDepartmentDataTest {

  @Autowired
  DepartmentRepository departmentRepository;
  @Autowired
  HotelGroupRepository hotelGroupRepository;

  @Transactional
  public void generate() {

    if (departmentRepository.count() > 0) {
      return;
    }
    for (long hotel = 1L; hotel <= 5L; hotel++) {
      Object[][] departmentsDummy = {
          {"경영",hotel},
          {"지원", hotel},
          {"객실", hotel},
          {"식음", hotel},
          {"조리", hotel},
          {"세일즈", hotel},
          {"홍보", hotel},
          {"시설", hotel},
      };

      for (Object[] departmentDummy : departmentsDummy) {
        Department department = Department.createDepartment(
            (String) departmentDummy[0],
            hotelGroupRepository.findById((long) departmentDummy[1]).orElseThrow()
        );
        departmentRepository.save(department);

      }
    }
  }

}
