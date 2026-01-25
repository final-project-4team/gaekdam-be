package com.gaekdam.gaekdambe.dummy.generate.operation_service.facility;

import com.gaekdam.gaekdambe.operation_service.facility.command.domain.entity.Facility;
import com.gaekdam.gaekdambe.operation_service.facility.command.domain.entity.FacilityUsage;
import com.gaekdam.gaekdambe.operation_service.facility.command.domain.enums.FacilityUsageType;
import com.gaekdam.gaekdambe.operation_service.facility.command.domain.enums.PriceSource;
import com.gaekdam.gaekdambe.operation_service.facility.command.infrastructure.repository.FacilityRepository;
import com.gaekdam.gaekdambe.operation_service.facility.command.infrastructure.repository.FacilityUsageRepository;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.domain.entity.PackageFacility;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.infrastructure.repository.PackageFacilityRepository;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.infrastructure.repository.ReservationRepository;
import com.gaekdam.gaekdambe.reservation_service.stay.command.domain.entity.Stay;
import com.gaekdam.gaekdambe.reservation_service.stay.command.infrastructure.repository.StayRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Component
public class DummyFacilityUsageDataTest {

    private static final int BATCH = 500;

    @Autowired FacilityUsageRepository facilityUsageRepository;
    @Autowired StayRepository stayRepository;
    @Autowired FacilityRepository facilityRepository;
    @Autowired ReservationRepository reservationRepository;
    @Autowired PackageFacilityRepository packageFacilityRepository;
    @Autowired
    EntityManager em;

    @Transactional
    public void generate() {

        // 중복 생성 방지
        if (facilityUsageRepository.count() > 0) return;

        Random random = new Random();

        // Stay 전체 로드 (투숙 기준)
        List<Stay> stays = stayRepository.findAll();
        if (stays.isEmpty()) return;

        /**
         * packageCode → 포함된 facilityCode 목록
         * (패키지 포함 시설 판별용 캐시)
         */
        Map<Long, List<Long>> packageFacilityMap =
                packageFacilityRepository.findAll()
                        .stream()
                        .collect(Collectors.groupingBy(
                                PackageFacility::getPackageCode,
                                Collectors.mapping(
                                        PackageFacility::getFacilityCode,
                                        Collectors.toList()
                                )
                        ));

        // 배치 저장 버퍼
        List<FacilityUsage> buffer = new ArrayList<>(BATCH);

        for (Stay stay : stays) {

            // 실제 체크인한 투숙만 대상
            if (stay.getActualCheckinAt() == null) continue;

            // 예약 정보 조회 (property / package)
            Long propertyCode =
                    reservationRepository.findPropertyCodeByReservationCode(
                            stay.getReservationCode()
                    );
            if (propertyCode == null) continue;

            Long packageCode =
                    reservationRepository.findPackageCodeByReservationCode(
                            stay.getReservationCode()
                    );

            // 패키지에 포함된 시설 목록
            List<Long> includedFacilities =
                    packageCode != null
                            ? packageFacilityMap.getOrDefault(packageCode, List.of())
                            : List.of();

            // 지점 전체 시설
            List<Facility> facilities =
                    facilityRepository.findByPropertyCode(propertyCode);
            if (facilities.isEmpty()) continue;

            // 투숙당 1~3회 시설 이용
            int usageCount = random.nextInt(3) + 1;
            boolean isStaying = stay.getActualCheckoutAt() == null;

            for (int i = 0; i < usageCount; i++) {

                Facility facility =
                        facilities.get(random.nextInt(facilities.size()));

                boolean isPersonBased = random.nextBoolean();

                // 이용 시각 결정
                LocalDateTime usageAt;
                if (isStaying) {
                    LocalDate today = LocalDate.now();
                    LocalDate checkinDate = stay.getActualCheckinAt().toLocalDate();

                    usageAt = checkinDate.isEqual(today)
                            ? randomDateTimeBetween(
                            stay.getActualCheckinAt(),
                            LocalDateTime.now(),
                            random
                    )
                            : randomDateTimeBetween(
                            today.atStartOfDay(),
                            LocalDateTime.now(),
                            random
                    );
                } else {
                    usageAt = randomDateTimeBetween(
                            stay.getActualCheckinAt(),
                            stay.getActualCheckoutAt(),
                            random
                    );
                }

                // 인원/수량 설정
                Integer usedPersonCount =
                        isPersonBased ? random.nextInt(4) + 1 : null;

                Integer usageQuantity =
                        isPersonBased ? null : random.nextInt(3) + 1;

                FacilityUsageType usageType =
                        (usedPersonCount != null && usedPersonCount >= 2)
                                ? FacilityUsageType.WITH_GUEST
                                : FacilityUsageType.PERSONAL;

                // 패키지 사용 가능 여부
                boolean usePackage =
                        packageCode != null
                                && includedFacilities.contains(facility.getFacilityCode())
                                && random.nextInt(100) < 70;

                FacilityUsage usage;

                if (usePackage) {
                    // 패키지 포함 시설 → 무료
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
                    // 추가 이용 → 유료
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

                buffer.add(usage);

                // 배치 저장
                if (buffer.size() == BATCH) {
                    facilityUsageRepository.saveAll(buffer);
                    em.flush();
                    em.clear();
                    buffer.clear();
                }
            }
        }

        // 남은 데이터 처리
        if (!buffer.isEmpty()) {
            facilityUsageRepository.saveAll(buffer);
            em.flush();
            em.clear();
        }
    }

    // start ~ end 사이 랜덤 시각
    private LocalDateTime randomDateTimeBetween(
            LocalDateTime start,
            LocalDateTime end,
            Random random
    ) {
        long seconds = java.time.Duration.between(start, end).getSeconds();
        if (seconds <= 0) return start;
        return start.plusSeconds(random.nextLong(seconds));
    }
}