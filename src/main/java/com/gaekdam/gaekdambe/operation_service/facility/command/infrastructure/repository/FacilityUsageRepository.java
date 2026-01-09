package com.gaekdam.gaekdambe.operation_service.facility.command.infrastructure.repository;

import com.gaekdam.gaekdambe.operation_service.facility.command.domain.entity.FacilityUsage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FacilityUsageRepository extends JpaRepository<FacilityUsage,Long> {
}
