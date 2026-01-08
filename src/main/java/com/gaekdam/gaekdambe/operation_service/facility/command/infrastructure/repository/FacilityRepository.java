package com.gaekdam.gaekdambe.operation_service.facility.command.infrastructure.repository;

import com.gaekdam.gaekdambe.operation_service.facility.command.domain.entity.Facility;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FacilityRepository extends JpaRepository<Facility,Long> {

}
