package com.gaekdam.gaekdambe.communication_service.incident.command.infrastructure.repository;

import com.gaekdam.gaekdambe.communication_service.incident.command.domain.entity.Incident;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentRepository extends JpaRepository<Incident,Long> {
}
