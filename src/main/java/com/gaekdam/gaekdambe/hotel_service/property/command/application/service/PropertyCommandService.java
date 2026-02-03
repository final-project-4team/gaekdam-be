package com.gaekdam.gaekdambe.hotel_service.property.command.application.service;

import com.gaekdam.gaekdambe.global.exception.CustomException;
import com.gaekdam.gaekdambe.global.exception.ErrorCode;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.domain.entity.HotelGroup;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.infrastructure.repository.HotelGroupRepository;
import com.gaekdam.gaekdambe.hotel_service.property.command.application.dto.request.PropertyRequest;
import com.gaekdam.gaekdambe.hotel_service.property.command.domain.PropertyStatus;
import com.gaekdam.gaekdambe.hotel_service.property.command.domain.entity.Property;
import com.gaekdam.gaekdambe.hotel_service.property.command.infrastructure.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PropertyCommandService {
  private final PropertyRepository propertyRepository;
  private final HotelGroupRepository hotelGroupRepository;
  @Transactional
  public String deleteProperty(Long propertyCode) {
    Property property= propertyRepository.findById(propertyCode).orElseThrow(()->new IllegalArgumentException("Property code not found"));
    if(property.getPropertyStatus()== PropertyStatus.CLOSED){
      throw new CustomException(ErrorCode.INVALID_REQUEST);
    }
    property.deleteProperty();
    return "삭제 되었습니다";

  }

  @Transactional
  public String createProperty(PropertyRequest request,Long hotelCode) {
    if(request.propertyName()==null || request.propertyName().isEmpty()){
      throw new CustomException(ErrorCode.INVALID_INCORRECT_FORMAT);
    }
    if(request.propertyCity()==null || request.propertyCity().isEmpty()){
      throw new CustomException(ErrorCode.INVALID_INCORRECT_FORMAT);
    }
    HotelGroup hotelGroup=hotelGroupRepository.findById(hotelCode).orElseThrow(()->new IllegalArgumentException("Hotel code not found"));

    Property property = Property.createProperty(
        request.propertyName(),
        request.propertyCity(),
        hotelGroup
    );
    propertyRepository.save(property);
    return "지점 추가 성공";
  }

  @Transactional
  public String updateProperty(Long propertyCode, PropertyRequest request) {
    if((request.propertyName()==null || request.propertyName().isEmpty())&&(request.propertyCity()==null || request.propertyCity().isEmpty())){
      throw new CustomException(ErrorCode.INVALID_INCORRECT_FORMAT);
    }
    Property property= propertyRepository.findById(propertyCode).orElseThrow(()->new IllegalArgumentException("Property code not found"));
    property.updateProperty(
        request.propertyName(),
        request.propertyCity()
    );
    return "지점 정보가 수정 되었습니다";
  }
}
