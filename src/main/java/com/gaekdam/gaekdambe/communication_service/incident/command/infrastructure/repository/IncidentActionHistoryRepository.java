package com.gaekdam.gaekdambe.communication_service.incident.command.infrastructure.repository;

import com.gaekdam.gaekdambe.communication_service.incident.command.domain.entity.IncidentActionHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentActionHistoryRepository extends JpaRepository<IncidentActionHistory, Long> {
}
