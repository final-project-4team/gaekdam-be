package com.gaekdam.gaekdambe.iam_service.employee.command.infrastructure;

import com.gaekdam.gaekdambe.iam_service.employee.command.domain.EmployeeStatus;
import com.gaekdam.gaekdambe.iam_service.employee.command.domain.entity.Employee;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

  Optional<Employee> findByLoginId(String employeeId);

  boolean existsByLoginId(String loginId);

  boolean existsByPhoneNumberHash(byte[] phoneNumberHash);

  List<Employee> findByEmployeeStatusAndLastLoginAtBefore(EmployeeStatus status, LocalDateTime targetDate);

  @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true)
  @org.springframework.data.jpa.repository.Query("UPDATE Employee e SET e.permission.permissionCode = :newPermissionCode WHERE e.permission.permissionCode = :oldPermissionCode")
  void bulkUpdatePermission(Long oldPermissionCode, Long newPermissionCode);
}
