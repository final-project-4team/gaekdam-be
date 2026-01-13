package com.gaekdam.gaekdambe.hotel_service.property.query.controller;

import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.hotel_service.property.query.dto.request.PropertyQueryRequest;
import com.gaekdam.gaekdambe.hotel_service.property.query.dto.response.PropertyListResponse;
import com.gaekdam.gaekdambe.hotel_service.property.query.service.PropertyQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/property")
public class PropertyQueryController {
  private final PropertyQueryService propertyQueryService;

  @GetMapping("")
  public ApiResponse<PageResponse<PropertyListResponse>> getPropertyList(
     PropertyQueryRequest query
  ){
    return ApiResponse.success(propertyQueryService.getPropertyList(query));
  }


}
