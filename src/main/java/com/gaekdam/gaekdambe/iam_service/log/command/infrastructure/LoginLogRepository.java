package com.gaekdam.gaekdambe.iam_service.log.command.infrastructure;

import com.gaekdam.gaekdambe.iam_service.employee.command.domain.entity.Employee;
import com.gaekdam.gaekdambe.iam_service.log.command.domain.entity.LoginLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginLogRepository extends JpaRepository<LoginLog, Long> {

    // 특정 직원의 로그인 이력 조회 (최신순)
    List<LoginLog> findByEmployeeOrderByOccurredAtDesc(Employee employee);

    // 특정 IP의 로그인 이력 조회
    List<LoginLog> findByUserIp(String userIp);
}
