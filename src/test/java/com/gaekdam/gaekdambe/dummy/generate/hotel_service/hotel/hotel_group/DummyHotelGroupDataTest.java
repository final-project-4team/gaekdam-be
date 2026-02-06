package com.gaekdam.gaekdambe.dummy.generate.hotel_service.hotel.hotel_group;

import com.gaekdam.gaekdambe.hotel_service.hotel.command.domain.entity.HotelGroup;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.infrastructure.repository.HotelGroupRepository;

import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DummyHotelGroupDataTest {

  @Autowired
  private HotelGroupRepository hotelGroupRepository;

  @Transactional
  public void generate() {

      if (hotelGroupRepository.count() > 0) {
          return;
      }

    Object[][] hotels = {
        {"한화 호텔", LocalDateTime.of(2027,1,3,12,0,0)},
        {"호텔 신라",LocalDateTime.of(2027,2,3,12,0,0)},
        {"호텔 롯데",LocalDateTime.of(2027,1,4,12,0,0)},
        {"앰배서더 호텔",LocalDateTime.of(2027,1,5,12,0,0)},
        {"워커힐 호텔",LocalDateTime.of(2027,1,6,12,0,0)},
        {"라한 호텔",LocalDateTime.of(2026,1,19,12,0,0)},
        {"코모도 호텔",LocalDateTime.of(2026,2,11,12,0,0)},
        {"파르나스 호텔",LocalDateTime.of(2026,3,3,12,0,0)},
        {"소노 호텔",LocalDateTime.of(2027,1,3,12,0,0)},
        {"켄싱턴 호텔",LocalDateTime.of(2026,5,3,12,0,0)},

    };

    for (Object[] hotel : hotels) {
      HotelGroup hotelGroup = HotelGroup.createHotelGroup(
          (String) hotel[0],
          (LocalDateTime) hotel[1]
      );
      hotelGroupRepository.save(hotelGroup);
    }
  }
}
