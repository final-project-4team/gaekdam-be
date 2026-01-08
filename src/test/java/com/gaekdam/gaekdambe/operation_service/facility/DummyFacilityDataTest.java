package com.gaekdam.gaekdambe.operation_service.facility;

import com.gaekdam.gaekdambe.operation_service.facility.command.domain.entity.Facility;
import com.gaekdam.gaekdambe.operation_service.facility.command.infrastructure.repository.FacilityRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.annotation.Rollback;

@SpringBootTest
@Transactional
@Rollback(false) // 실제 DB에 반영시킨다
public class DummyFacilityDataTest {

    @Autowired
    private FacilityRepository facilityRepository;

    @Test
    @DisplayName("각 호텔별로 부대시설 5개씩")
    void createFacilities() {

        // 모든 호텔 공통 (4개)
        String[][] baseFacilities = {
                {"조식 뷔페", "식사", "07:00~10:00"},
                {"피트니스 센터", "운동", "06:00~23:00"},
                {"사우나", "휴식", "10:00~22:00"},
                {"노래방", "여가", "24H"}
        };

        // 선택 프리미엄 시설 (1개만 추가)
        String[][] premiumFacilities = {
                {"수영장", "레저", "09:00~21:00"},
                {"키즈존", "여가", "09:00~18:00"},
                {"라운지 바", "식사", "18:00~02:00"}
        };

        for (long hotelCode = 1; hotelCode <= 10; hotelCode++) {

            // 공통 시설 4개
            for (String[] f : baseFacilities) {
                facilityRepository.save(
                        Facility.createFacility(
                                f[0],
                                f[1],
                                f[2],
                                "ACTIVE",
                                hotelCode
                        )
                );
            }

            // 호텔별 프리미엄 시설 1개 선택
            int index = (int) ((hotelCode - 1) % premiumFacilities.length);
            String[] premium = premiumFacilities[index];

            facilityRepository.save(
                    Facility.createFacility(
                            premium[0],
                            premium[1],
                            premium[2],
                            "ACTIVE",
                            hotelCode
                    )
            );
        }
    }
}
