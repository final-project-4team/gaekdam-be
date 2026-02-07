package com.gaekdam.gaekdambe.dummy.generate.hotel_service.position;

import com.gaekdam.gaekdambe.hotel_service.department.command.infrastructure.DepartmentRepository;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.infrastructure.repository.HotelGroupRepository;
import com.gaekdam.gaekdambe.hotel_service.position.command.domain.entity.HotelPosition;
import com.gaekdam.gaekdambe.hotel_service.position.command.infrastructure.repository.HotelPositionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DummyPositionDataTest {

  @Autowired
  private HotelPositionRepository hotelPositionRepository;
  @Autowired
  private HotelGroupRepository hotelGroupRepository;
  @Autowired
  private DepartmentRepository departmentRepository;

  @Transactional
  public void generate() {

    if (hotelPositionRepository.count() > 0) {
      return;
    }
    hotelPositionRepository.save(HotelPosition.createHotelPosition(
        "더미용 직급",
        departmentRepository.findById((long)1L).orElseThrow(),
        hotelGroupRepository.findById((long) 10L).orElseThrow())
    );

    for (long hotel = 1L; hotel <= 5L; hotel++) {
      Object[][] positionsDummy = {
          {"총지배인", 1L+(hotel-1)*8, hotel},
          {"부지배인", 1L+(hotel-1)*8, hotel},
          {"회계부장", 2L+(hotel-1)*8, hotel},
          {"회계부 사원", 2L+(hotel-1)*8, hotel},
          {"하우스 키핑 매니저", 3L+(hotel-1)*8, hotel},
          {"청소 직원", 3L+(hotel-1)*8, hotel},
          {"레스토랑 매니저", 4L+(hotel-1)*8, hotel},
          {"연회 매니저", 4L+(hotel-1)*8, hotel},
          {"주방장", 5L+(hotel-1)*8, hotel},
          {"조리사", 5L+(hotel-1)*8, hotel},
          {"세일즈 매니저", 6L+(hotel-1)*8, hotel},
          {"세일즈 디렉터", 6L+(hotel-1)*8, hotel},
          {"마케팅 매니저", 7L+(hotel-1)*8, hotel},
          {"브랜드 매니저", 7L+(hotel-1)*8, hotel},
          {"시설 팀장", 8L+(hotel-1)*8, hotel},
          {"난방 기사", 8L+(hotel-1)*8, hotel},
      };
      for (Object[] positionDummy : positionsDummy) {
        HotelPosition position = HotelPosition.createHotelPosition(
            (String) positionDummy[0],
            departmentRepository.findById((long) positionDummy[1]).orElseThrow(),
            hotelGroupRepository.findById((long) positionDummy[2]).orElseThrow()
        );

        hotelPositionRepository.save(position);
      }
    }
  }
}
