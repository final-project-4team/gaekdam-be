package com.gaekdam.gaekdambe.hotel_service.property.command.infrastructure;

import com.gaekdam.gaekdambe.hotel_service.property.command.domain.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {

}
