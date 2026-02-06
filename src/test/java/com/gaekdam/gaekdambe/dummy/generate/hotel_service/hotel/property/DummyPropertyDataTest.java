package com.gaekdam.gaekdambe.dummy.generate.hotel_service.hotel.property;

import com.gaekdam.gaekdambe.hotel_service.property.command.domain.PropertyStatus;
import com.gaekdam.gaekdambe.hotel_service.property.command.domain.entity.Property;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.infrastructure.repository.HotelGroupRepository;
import com.gaekdam.gaekdambe.hotel_service.property.command.infrastructure.PropertyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DummyPropertyDataTest {

  @Autowired
  private PropertyRepository propertyRepository;
  @Autowired
  private HotelGroupRepository hotelGroupRepository;

  @Transactional
  public void generate() {

      if (propertyRepository.count() > 0) {
          return;
      }

    Object[][] properties = {
        {"한화리조트 경주", PropertyStatus.ACTIVE, "경주", (long)1},
        {"한화리조트 설악", PropertyStatus.ACTIVE, "설악", (long)1},
        {"한화리조트 용인", PropertyStatus.ACTIVE, "용인", (long)1},
        {"한화리조트 해운대", PropertyStatus.ACTIVE, "해운대", (long)1},
        {"한화리조트 제주", PropertyStatus.ACTIVE, "제주", (long)1},
        {"서울 신라 호텔", PropertyStatus.ACTIVE, "서울", (long)2},
        {"제주 신라 호텔", PropertyStatus.ACTIVE, "제주", (long)2},
        {"부산 신라 호텔", PropertyStatus.ACTIVE, "서울", (long)2},
        {"인천 신라 호텔", PropertyStatus.ACTIVE, "제주", (long)2},
        {"대구 신라 호텔", PropertyStatus.ACTIVE, "서울", (long)2},
        {"시그니엘 서울", PropertyStatus.ACTIVE, "서울", (long)3},
        {"시그니엘 부산", PropertyStatus.ACTIVE, "부산", (long)3},
        {"롯데 호텔 서울", PropertyStatus.ACTIVE, "서울", (long)3},
        {"롯데 호텔 제주", PropertyStatus.ACTIVE, "제주", (long)3},
        {"롯데 호텔 울산", PropertyStatus.ACTIVE, "울산", (long)3},
        {"앰버서더 서울 풀만", PropertyStatus.ACTIVE, "서울", (long)4},
        {"노보텔 앰배서더 서울 강남", PropertyStatus.ACTIVE, "서울", (long)4},
        {"머큐어 앰배서더 서울 홍대", PropertyStatus.ACTIVE, "서울", (long)4},
        {"이비스 앰배서더 서울 명동", PropertyStatus.ACTIVE, "서울", (long)4},
        {"이비스 앰배서더 서울 인사동", PropertyStatus.ACTIVE, "서울", (long)4},
        {"그랜드 워커힐", PropertyStatus.ACTIVE, "서울", (long)5},
        {"비스타 워커힐", PropertyStatus.ACTIVE, "서울", (long)5},
        {"더글라스 하우스", PropertyStatus.ACTIVE, "서울", (long)5},
        {"퍼스트 워커힐", PropertyStatus.ACTIVE, "서울", (long)5},
        {"파스타 워커힐", PropertyStatus.ACTIVE, "서울", (long)5},

    };


    for (Object[] objects : properties) {
      Property property = Property.createProperty(
          (String) objects[0],
          (String) objects[2],
          hotelGroupRepository.findById((long) objects[3]).orElse(null)
      );
      propertyRepository.save(property);
    }

  }
}