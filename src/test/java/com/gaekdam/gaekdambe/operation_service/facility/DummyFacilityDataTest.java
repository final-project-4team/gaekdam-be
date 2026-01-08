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
    @DisplayName("부대시설 더미데이터 10개 생성")
    void createFacilityDummy() {

        String[][] facilities = {
                {"조식 뷔페", "DINING", "07:00~10:00", "ACTIVE"},
                {"수영장", "EXERCISE", "09:00~21:00", "ACTIVE"},
                {"피트니스 센터", "EXERCISE", "06:00~23:00", "ACTIVE"},
                {"사우나", "REST", "10:00~22:00", "ACTIVE"},
                {"키즈존", "RECREATION", "09:00~18:00", "ACTIVE"},
                {"비즈니스 라운지", "DINING", "24H", "ACTIVE"},
                {"라운지 바", "DINING", "18:00~02:00", "ACTIVE"},
                {"세탁실", "LEISURE", "24H", "ACTIVE"},
                {"회의실", "LEISURE", "09:00~20:00", "ACTIVE"},
                {"오락실", "RECREATION", "24H", "ACTIVE"}
        };

        for (String[] f : facilities) {
            Facility facility = Facility.createFacility(
                    f[0], // name
                    f[1], // type
                    f[2], // hours
                    f[3]  // status
            );

            facilityRepository.save(facility);
        }
    }
}
