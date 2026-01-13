package com.gaekdam.gaekdambe.hotel_service.property.query.dto.request;

import com.gaekdam.gaekdambe.hotel_service.property.command.domain.PropertyStatus;

public record PropertyQueryRequest(
    // Page
    Integer page,
    Integer size,
    // Search
    Long propertyCode,
    String propertyCity,
    String propertyName,
    PropertyStatus propertyStatus,
    // Sort
    String sortBy,
    String direction) {

  public PropertyQueryRequest {
    if (page == null)
      page = 1;
    if (size == null)
      size = 20;
    if (sortBy == null)
      sortBy = "property_code";
    if (direction == null)
      direction = "DESC";
  }
}