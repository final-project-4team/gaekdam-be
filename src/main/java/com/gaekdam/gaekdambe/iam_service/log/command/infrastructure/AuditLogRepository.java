package com.gaekdam.gaekdambe.iam_service.log.command.infrastructure;

import com.gaekdam.gaekdambe.iam_service.log.command.domain.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
