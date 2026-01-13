package com.gaekdam.gaekdambe.hotel_service.property.command.domain.entity;

import com.gaekdam.gaekdambe.hotel_service.property.command.domain.PropertyStatus;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.domain.entity.HotelGroup;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "property")
public class Property {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name="property_code")
  private Long propertyCode;

  @Column(name="property_name",length = 20,nullable = false)
  private String propertyName;


  @Enumerated(EnumType.STRING)
  @Column(name="property_status",nullable = false)
  private PropertyStatus propertyStatus;

  @Column(name="property_city",nullable = false)
  private String propertyCity;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "hotel_group_code",nullable = false)
  private HotelGroup hotelGroup;


  public static Property createProperty( String propertyName, String propertyCity, HotelGroup hotelGroup) {
    return Property.builder()
        .propertyName(propertyName)
        .propertyStatus(PropertyStatus.ACTIVE)
        .propertyCity(propertyCity)
        .hotelGroup(hotelGroup)
        .build();
  }
  public void updateProperty(String propertyName, String propertyCity) {
    this.propertyName = propertyName;
    this.propertyCity=propertyCity;
  }
  public void deleteProperty() {
    this.propertyStatus=PropertyStatus.CLOSED;
  }
}
