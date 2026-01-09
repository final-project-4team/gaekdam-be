package com.gaekdam.gaekdambe.dummy.operation_service.facility;

import com.gaekdam.gaekdambe.operation_service.facility.command.domain.entity.FacilityUsage;
import com.gaekdam.gaekdambe.operation_service.facility.command.infrastructure.repository.FacilityUsageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;

@SpringBootTest
@Transactional
@Rollback(false)
public class DummyFacilityUsageDataTest {

    @Autowired
    private FacilityUsageRepository facilityUsageRepository;

    @Test
    @DisplayName("부대시설 이용내역 2만건 생성")
    void createFacilityUsageDummy() {

        Random random = new Random();
        int totalCount = 20_000;

        for (int i = 1; i <= totalCount; i++) {

            long stayCode = random.nextInt(8000) + 1;     // 투숙 1~8000
            long facilityCode = random.nextInt(50) + 1;  // 시설 1~50

            boolean isPackage = random.nextInt(100) < 65; // 65% 패키지
            boolean isPersonBased = random.nextInt(100) < 70; // 70% 인원형

            FacilityUsage usage;

            if (isPackage) {
                // 패키지 포함 이용 (금액 0)
                usage = FacilityUsage.builder()
                        .stayCode(stayCode)
                        .facilityCode(facilityCode)
                        .usageAt(LocalDateTime.now().minusDays(random.nextInt(60)))
                        .usageType("PERSONAL")
                        .usedPersonCount(isPersonBased ? random.nextInt(4) + 1 : null)
                        .usageQuantity(isPersonBased ? null : random.nextInt(2) + 1)
                        .usagePrice(BigDecimal.ZERO)
                        .priceSource("PACKAGE")
                        .createdAt(LocalDateTime.now())
                        .build();
            } else {
                // 추가 결제
                int quantity = isPersonBased ? 1 : random.nextInt(3) + 1;
                BigDecimal unitPrice = BigDecimal.valueOf(
                        (random.nextInt(5) + 1) * 10000 // 1~5만원
                );

                usage = FacilityUsage.builder()
                        .stayCode(stayCode)
                        .facilityCode(facilityCode)
                        .usageAt(LocalDateTime.now().minusDays(random.nextInt(60)))
                        .usageType("PERSONAL")
                        .usedPersonCount(isPersonBased ? random.nextInt(4) + 1 : null)
                        .usageQuantity(isPersonBased ? null : quantity)
                        .usagePrice(unitPrice.multiply(BigDecimal.valueOf(quantity)))
                        .priceSource("EXTRA")
                        .createdAt(LocalDateTime.now())
                        .build();
            }

            facilityUsageRepository.save(usage);
        }
    }
}
