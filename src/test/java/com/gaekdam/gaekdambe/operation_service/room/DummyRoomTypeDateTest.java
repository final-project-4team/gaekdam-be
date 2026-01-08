package com.gaekdam.gaekdambe.operation_service.room;

import com.gaekdam.gaekdambe.operation_service.room.command.domain.entity.RoomType;
import com.gaekdam.gaekdambe.operation_service.room.command.infrastructure.repository.RoomTypeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@SpringBootTest
@Transactional
@Rollback(false) // 실제 DB에 반영시킨다
public class DummyRoomTypeDateTest {

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    @Test
    @DisplayName("룸타입 더미데이터 20개 생성")
    void createRoomTypeDummy(){

        Object[][] roomTypes = {
                {"스탠다드", 2, "DOUBLE", "CITY", new BigDecimal("120000"), "기본 객실"},
                {"스탠다드 오션", 2, "DOUBLE", "OCEAN", new BigDecimal("140000"), "오션뷰 기본 객실"},
                {"스탠다드 레이크", 2, "DOUBLE", "LAKE", new BigDecimal("135000"), "레이크뷰 기본 객실"},

                {"디럭스", 2, "QUEEN", "CITY", new BigDecimal("160000"), "여유 있는 디럭스 객실"},
                {"디럭스 오션", 2, "QUEEN", "OCEAN", new BigDecimal("180000"), "오션뷰 디럭스"},
                {"디럭스 레이크", 2, "QUEEN", "LAKE", new BigDecimal("175000"), "레이크뷰 디럭스"},

                {"프리미엄 디럭스", 2, "KING", "CITY", new BigDecimal("210000"), "프리미엄 디럭스"},
                {"프리미엄 디럭스 오션", 2, "KING", "OCEAN", new BigDecimal("240000"), "프리미엄 오션 디럭스"},

                {"패밀리", 4, "DOUBLE+SINGLE", "CITY", new BigDecimal("200000"), "가족 고객용 객실"},
                {"패밀리 오션", 4, "DOUBLE+SINGLE", "OCEAN", new BigDecimal("230000"), "가족 오션뷰 객실"},
                {"패밀리 레이크", 4, "DOUBLE+SINGLE", "LAKE", new BigDecimal("225000"), "가족 레이크뷰 객실"},

                {"패밀리 프리미엄", 5, "DOUBLE+DOUBLE", "CITY", new BigDecimal("260000"), "대가족 프리미엄 객실"},

                {"스위트", 2, "KING", "CITY", new BigDecimal("280000"), "고급 스위트"},
                {"스위트 오션", 2, "KING", "OCEAN", new BigDecimal("320000"), "오션뷰 스위트"},
                {"스위트 레이크", 2, "KING", "LAKE", new BigDecimal("310000"), "레이크뷰 스위트"},

                {"프리미엄 스위트", 2, "KING", "CITY", new BigDecimal("350000"), "프리미엄 스위트"},
                {"프리미엄 스위트 오션", 2, "KING", "OCEAN", new BigDecimal("380000"), "프리미엄 오션 스위트"},

                {"로얄 스위트", 4, "KING+DOUBLE", "CITY", new BigDecimal("420000"), "최상급 객실"},
                {"로얄 스위트 오션", 4, "KING+DOUBLE", "OCEAN", new BigDecimal("450000"), "최상급 오션뷰 객실"},
                {"로얄 스위트 레이크", 4, "KING+DOUBLE", "LAKE", new BigDecimal("440000"), "최상급 레이크뷰 객실"}
        };

        for (Object[] r : roomTypes) {
            RoomType roomType = RoomType.createRoomType(
                    (String) r[0],
                    (Integer) r[1],
                    (String) r[2],
                    (String) r[3],
                    (BigDecimal) r[4],
                    (String) r[5]
            );

            roomTypeRepository.save(roomType);
        }

    }
}
