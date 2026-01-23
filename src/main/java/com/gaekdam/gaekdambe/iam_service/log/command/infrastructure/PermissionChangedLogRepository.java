package com.gaekdam.gaekdambe.iam_service.log.command.infrastructure;

import com.gaekdam.gaekdambe.iam_service.employee.command.domain.entity.Employee;
import com.gaekdam.gaekdambe.iam_service.log.command.domain.entity.PermissionChangedLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionChangedLogRepository extends JpaRepository<PermissionChangedLog, Long> {

    // 특정 직원의 권한 변경 이력 조회 (변경된 사용자 기준)
    List<PermissionChangedLog> findByEmployeeChangedOrderByChangedAtDesc(Employee employee);

    // 특정 직원이 변경한 이력 조회 (변경자 기준)
    List<PermissionChangedLog> findByEmployeeAccessorOrderByChangedAtDesc(Employee employee);
}
