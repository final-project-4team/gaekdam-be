package com.gaekdam.gaekdambe.hotel_service.department.command.infrastructure;

import com.gaekdam.gaekdambe.hotel_service.department.command.domain.entity.Department;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;


public interface DepartmentRepository extends JpaRepository<Department, Long> {

  List<Department> findByHotelGroup_HotelGroupCode(Long hotelGroupCode);
}
