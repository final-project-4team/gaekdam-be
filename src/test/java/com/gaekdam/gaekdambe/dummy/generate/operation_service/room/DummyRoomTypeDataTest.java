package com.gaekdam.gaekdambe.dummy.generate.operation_service.room;

import com.gaekdam.gaekdambe.operation_service.room.command.domain.entity.RoomType;
import com.gaekdam.gaekdambe.operation_service.room.command.infrastructure.repository.RoomTypeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
public class DummyRoomTypeDataTest {

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    @Transactional
    public void generate() {

        if (roomTypeRepository.count() > 0) {
            return;
        }

        // 모든 호텔 공통 (3개)
        Object[][] baseRoomTypes = {
                {"스탠다드", 2, "DOUBLE", "CITY", 120000, "기본 객실"},
                {"디럭스", 2, "QUEEN", "CITY", 160000, "디럭스 객실"},
                {"패밀리", 4, "DOUBLE+SINGLE", "CITY", 200000, "가족 객실"}
        };

        // 짝수 호텔용 (2개)
        Object[][] oceanRoomTypes = {
                {"디럭스 오션", 2, "QUEEN", "OCEAN", 190000, "오션뷰 디럭스"},
                {"패밀리 오션", 4, "DOUBLE+SINGLE", "OCEAN", 230000, "오션뷰 패밀리"}
        };

        // 홀수 호텔용 (2개)
        Object[][] suiteRoomTypes = {
                {"스위트", 2, "KING", "CITY", 280000, "스위트 객실"},
                {"로얄 스위트", 4, "KING+DOUBLE", "OCEAN", 450000, "최상급 스위트"}
        };

        for (long propertyCode = 1; propertyCode <= 20; propertyCode++) {

            // 공통 3개
            for (Object[] r : baseRoomTypes) {
                roomTypeRepository.save(
                        RoomType.createRoomType(
                                (String) r[0],
                                (int) r[1],
                                (String) r[2],
                                (String) r[3],
                                BigDecimal.valueOf((int) r[4]),
                                (String) r[5],
                                propertyCode
                        )
                );
            }

            // 호텔별 추가 2개 → 총 5개 보장
            Object[][] extraTypes = (propertyCode % 2 == 0)
                    ? oceanRoomTypes
                    : suiteRoomTypes;

            for (Object[] r : extraTypes) {
                roomTypeRepository.save(
                        RoomType.createRoomType(
                                (String) r[0],
                                (int) r[1],
                                (String) r[2],
                                (String) r[3],
                                BigDecimal.valueOf((int) r[4]),
                                (String) r[5],
                                propertyCode
                        )
                );
            }
        }
    }

}
