package com.gaekdam.gaekdambe.dummy.hotel_service.position;

import com.gaekdam.gaekdambe.hotel_service.department.command.infrastructure.DepartmentRepository;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.infrastructure.repository.HotelGroupRepository;
import com.gaekdam.gaekdambe.hotel_service.position.command.domain.entity.HotelPosition;
import com.gaekdam.gaekdambe.hotel_service.position.command.infrastructure.repository.HotelPositionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@Rollback(false)
public class DummyPositionDataTest {

  @Autowired
  private HotelPositionRepository hotelPositionRepository;
  @Autowired
  private HotelGroupRepository hotelGroupRepository;
  @Autowired
  private DepartmentRepository departmentRepository;

  @Test
  @DisplayName("직책 생성")
  void createPosition() {
    Object[][] positionsDummy={
        {"총지배인",1L,1L},
        {"부지배인",1L,1L},
        {"지점장지배인",1L,1L},
        {"회계부장",2L,1L},
        {"회계부 사원",2L,1L},
        {"하우스 키핑 매니저",3L,1L},
        {"청소 직원",3L,1L},
        {"레스토랑 매니저",4L,1L},
        {"연회 매니저",4L,1L},
        {"서버",4L,1L},
        {"주방장",5L,1L},
        {"조리사",5L,1L},
        {"세일즈 매니저",6L,1L},
        {"세일즈 디렉터",6L,1L},
        {"마케팅 매니저",7L,1L},
        {"브랜드 매니저",7L,1L},
        {"시설 팀장",8L,1L},
        {"난방 기사",8L,1L},
    };
    for(Object[] positionDummy:positionsDummy){
      HotelPosition position = HotelPosition.createHotelPosition(
          (String)positionDummy[0],
          departmentRepository.findById((long)positionDummy[1]).orElseThrow(),
          hotelGroupRepository.findById((long)positionDummy[2]).orElseThrow()
      );

      hotelPositionRepository.save(position);
    }
  }
}
