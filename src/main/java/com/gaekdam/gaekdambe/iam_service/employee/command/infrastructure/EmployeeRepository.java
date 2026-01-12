package com.gaekdam.gaekdambe.iam_service.employee.command.infrastructure;

import com.gaekdam.gaekdambe.iam_service.employee.command.domain.entity.Employee;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

  // 해시 기반 검색 (암호화된 데이터 복호화 없이 검색 가능)
  Optional<Employee> findByEmailHash(byte[] emailHash);

  Optional<Employee> findByPhoneNumberHash(byte[] phoneNumberHash);

  Optional<Employee> findByEmployeeNameHash(byte[] employeeNameHash);

  Optional<Employee> findByLoginId(String employeeId);

  boolean existsByLoginId(String loginId);

  boolean existsByPhoneNumberHash(byte[] phoneNumberHash);
}
