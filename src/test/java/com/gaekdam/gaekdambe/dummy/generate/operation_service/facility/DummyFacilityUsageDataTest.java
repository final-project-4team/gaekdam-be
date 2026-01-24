package com.gaekdam.gaekdambe.dummy.generate.operation_service.facility;

import com.gaekdam.gaekdambe.operation_service.facility.command.domain.entity.Facility;
import com.gaekdam.gaekdambe.operation_service.facility.command.domain.entity.FacilityUsage;
import com.gaekdam.gaekdambe.operation_service.facility.command.domain.enums.FacilityUsageType;
import com.gaekdam.gaekdambe.operation_service.facility.command.domain.enums.PriceSource;
import com.gaekdam.gaekdambe.operation_service.facility.command.infrastructure.repository.FacilityRepository;
import com.gaekdam.gaekdambe.operation_service.facility.command.infrastructure.repository.FacilityUsageRepository;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.infrastructure.repository.ReservationRepository;
import com.gaekdam.gaekdambe.reservation_service.stay.command.domain.entity.Stay;
import com.gaekdam.gaekdambe.reservation_service.stay.command.infrastructure.repository.StayRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Component
public class DummyFacilityUsageDataTest {

    @Autowired
    private FacilityUsageRepository facilityUsageRepository;
    @Autowired
    private StayRepository stayRepository;
    @Autowired
    private FacilityRepository facilityRepository;
    @Autowired
    private ReservationRepository reservationRepository;

    @Transactional
    public void generate() {

        if (facilityUsageRepository.count() > 0) return;

        List<Stay> stays = stayRepository.findAll();
        if (stays.isEmpty()) return;

        Random random = new Random();

        for (Stay stay : stays) {

            //  투숙객만
            if (stay.getCustomerCode() == null) continue;
            if (stay.getReservationCode() == null) continue;
            if (stay.getActualCheckinAt() == null) continue;

            // reservation → propertyCode
            Long propertyCode =
                    reservationRepository.findPropertyCodeByReservationCode(
                            stay.getReservationCode()
                    );
            if (propertyCode == null) continue;

            // 지점별 시설
            List<Facility> facilities =
                    facilityRepository.findByPropertyCode(propertyCode);
            if (facilities.isEmpty()) continue;

            // 투숙당 1~5회 이용
            int usageCount = random.nextInt(5) + 1;

            LocalDateTime start = stay.getActualCheckinAt();
            LocalDateTime end =
                    stay.getActualCheckoutAt() != null
                            ? stay.getActualCheckoutAt()
                            : LocalDateTime.now();

            if (start.isAfter(end)) continue;

            for (int i = 0; i < usageCount; i++) {

                Facility facility =
                        facilities.get(random.nextInt(facilities.size()));

                boolean isPackage = random.nextInt(100) < 60;
                boolean isPersonBased = random.nextBoolean();

                LocalDateTime usageAt;

                if (stay.getActualCheckoutAt() == null) {
                    //  STAYING → 오늘 데이터 보장
                    LocalDateTime todayStart = LocalDateTime.now()
                            .toLocalDate()
                            .atStartOfDay();
                    LocalDateTime now = LocalDateTime.now();

                    // 체크인이 오늘 이후면 체크인~now
                    if (todayStart.isBefore(stay.getActualCheckinAt())) {
                        usageAt = randomDateTimeBetween(
                                stay.getActualCheckinAt(),
                                now,
                                random
                        );
                    } else {
                        // 오늘 구간에서 반드시 생성
                        usageAt = randomDateTimeBetween(
                                todayStart,
                                now,
                                random
                        );
                    }
                } else {
                    // COMPLETED → 체크인 ~ 체크아웃
                    usageAt = randomDateTimeBetween(
                            stay.getActualCheckinAt(),
                            stay.getActualCheckoutAt(),
                            random
                    );
                }

                FacilityUsage usage;

                Integer usedPersonCount =
                        isPersonBased ? random.nextInt(4) + 1 : null;

                Integer usageQuantity =
                        isPersonBased ? null : random.nextInt(3) + 1;

                FacilityUsageType usageType =
                        (usedPersonCount != null && usedPersonCount >= 2)
                                ? FacilityUsageType.WITH_GUEST
                                : FacilityUsageType.PERSONAL;

                if (isPackage) {
                    usage = FacilityUsage.builder()
                            .stayCode(stay.getStayCode())
                            .facilityCode(facility.getFacilityCode())
                            .usageAt(usageAt)
                            .usageType(usageType)
                            .usedPersonCount(usedPersonCount)
                            .usageQuantity(usageQuantity)
                            .usagePrice(BigDecimal.ZERO)
                            .priceSource(PriceSource.PACKAGE)
                            .createdAt(LocalDateTime.now())
                            .build();
                } else {
                    int quantity = usageQuantity != null ? usageQuantity : 1;
                    BigDecimal unitPrice =
                            BigDecimal.valueOf((random.nextInt(5) + 1) * 10_000);

                    usage = FacilityUsage.builder()
                            .stayCode(stay.getStayCode())
                            .facilityCode(facility.getFacilityCode())
                            .usageAt(usageAt)
                            .usageType(usageType)
                            .usedPersonCount(usedPersonCount)
                            .usageQuantity(usageQuantity)
                            .usagePrice(unitPrice.multiply(BigDecimal.valueOf(quantity)))
                            .priceSource(PriceSource.EXTRA)
                            .createdAt(LocalDateTime.now())
                            .build();
                }

                facilityUsageRepository.save(usage);
            }
        }
    }

    private LocalDateTime randomDateTimeBetween(
            LocalDateTime start,
            LocalDateTime end,
            Random random
    ) {
        long seconds =
                java.time.Duration.between(start, end).getSeconds();
        if (seconds <= 0) return start;

        return start.plusSeconds(random.nextLong(seconds));
    }
}
