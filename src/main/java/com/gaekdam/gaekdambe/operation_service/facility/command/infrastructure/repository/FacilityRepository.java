package com.gaekdam.gaekdambe.operation_service.facility.command.infrastructure.repository;

import com.gaekdam.gaekdambe.operation_service.facility.command.domain.entity.Facility;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FacilityRepository extends JpaRepository<Facility,Long> {

    List<Facility> findByPropertyCode(Long propertyCode);
}
