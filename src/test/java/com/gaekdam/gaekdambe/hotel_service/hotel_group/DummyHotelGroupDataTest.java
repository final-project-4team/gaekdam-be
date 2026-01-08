package com.gaekdam.gaekdambe.hotel_service.hotel_group;

import com.gaekdam.gaekdambe.hotel_service.hotel.command.domain.entity.HotelGroup;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.infrastructure.repository.HotelGroupRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

@SpringBootTest
@Transactional
@Rollback(false)
public class DummyHotelGroupDataTest {

  @Autowired
  private HotelGroupRepository hotelGroupRepository;

  @Test
  @DisplayName("호텔 그룹 생성")
  void createHotelGroupDataDummy() {

    Object[][] hotels = {
        {"한화 호텔", LocalDateTime.of(2027,1,3,12,0,0)},
        {"호텔 신라",LocalDateTime.of(2027,2,3,12,0,0)},
        {"호텔 롯데",LocalDateTime.of(2027,1,4,12,0,0)},
        {"앰배서더 호텔",LocalDateTime.of(2027,1,5,12,0,0)},
        {"워커힐 호텔",LocalDateTime.of(2027,1,6,12,0,0)},
        {"라한 호텔",LocalDateTime.of(2026,1,19,12,0,0)},
        {"코모도 호텔",LocalDateTime.of(2026,2,11,12,0,0)},
        {"파르나스 호텔",LocalDateTime.of(206,3,3,12,0,0)},
        {"소노 호텔",LocalDateTime.of(2027,1,3,12,0,0)},
        {"켄싱턴 호텔",LocalDateTime.of(2026,5,3,12,0,0)},

    };

    for ( int i=0; i<hotels.length; i++ ) {
      HotelGroup hotelGroup = HotelGroup.createHotelGroup(
          (String)hotels[i][0],
          (LocalDateTime) hotels[i][1]
      );
      hotelGroupRepository.save(hotelGroup);
    }
  }
}
