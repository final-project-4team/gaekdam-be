package com.gaekdam.gaekdambe.hotel_service.department.command.infrastructure;

import com.gaekdam.gaekdambe.hotel_service.department.command.domain.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;


public interface DepartmentRepository extends JpaRepository<Department, Long> {

}
