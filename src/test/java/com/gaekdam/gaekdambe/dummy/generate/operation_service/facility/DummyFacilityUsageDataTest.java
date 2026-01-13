package com.gaekdam.gaekdambe.dummy.generate.operation_service.facility;

import com.gaekdam.gaekdambe.operation_service.facility.command.domain.entity.FacilityUsage;
import com.gaekdam.gaekdambe.operation_service.facility.command.domain.enums.FacilityUsageType;
import com.gaekdam.gaekdambe.operation_service.facility.command.domain.enums.PriceSource;
import com.gaekdam.gaekdambe.operation_service.facility.command.infrastructure.repository.FacilityUsageRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;

@Component
public class DummyFacilityUsageDataTest {

    @Autowired
    private FacilityUsageRepository facilityUsageRepository;

    @Transactional
    public void generate() {

        if (facilityUsageRepository.count() > 0) {
            return;
        }

        Random random = new Random();
        int totalCount = 20_000;

        for (int i = 1; i <= totalCount; i++) {

            long stayCode = random.nextInt(8_000) + 1;
            long facilityCode = random.nextInt(50) + 1;

            boolean isPackage = random.nextInt(100) < 65;
            boolean isPersonBased = random.nextInt(100) < 70;

            FacilityUsage usage;

            if (isPackage) {
                // 패키지 포함 이용
                usage = FacilityUsage.builder()
                        .stayCode(stayCode)
                        .facilityCode(facilityCode)
                        .usageAt(LocalDateTime.now().minusDays(random.nextInt(60)))
                        .usageType(FacilityUsageType.PERSONAL)
                        .usedPersonCount(isPersonBased ? random.nextInt(4) + 1 : null)
                        .usageQuantity(isPersonBased ? null : random.nextInt(2) + 1)
                        .usagePrice(BigDecimal.ZERO)
                        .priceSource(PriceSource.PACKAGE)
                        .createdAt(LocalDateTime.now())
                        .build();
            } else {
                // 추가 결제
                int quantity = isPersonBased ? 1 : random.nextInt(3) + 1;
                BigDecimal unitPrice =
                        BigDecimal.valueOf((random.nextInt(5) + 1) * 10_000);

                usage = FacilityUsage.builder()
                        .stayCode(stayCode)
                        .facilityCode(facilityCode)
                        .usageAt(LocalDateTime.now().minusDays(random.nextInt(60)))
                        .usageType(FacilityUsageType.PERSONAL)
                        .usedPersonCount(isPersonBased ? random.nextInt(4) + 1 : null)
                        .usageQuantity(isPersonBased ? null : quantity)
                        .usagePrice(unitPrice.multiply(BigDecimal.valueOf(quantity)))
                        .priceSource(PriceSource.EXTRA)
                        .createdAt(LocalDateTime.now())
                        .build();
            }

            facilityUsageRepository.save(usage);
        }
    }
}
