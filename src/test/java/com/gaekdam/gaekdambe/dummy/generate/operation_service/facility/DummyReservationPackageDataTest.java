package com.gaekdam.gaekdambe.dummy.generate.operation_service.facility;

import com.gaekdam.gaekdambe.reservation_service.reservation.command.domain.entity.PackageFacility;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.domain.entity.ReservationPackage;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.infrastructure.repository.PackageFacilityRepository;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.infrastructure.repository.ReservationPackageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;

@Component
public class DummyReservationPackageDataTest {

    @Autowired
    private ReservationPackageRepository packageRepository;

    @Autowired
    private PackageFacilityRepository packageFacilityRepository;

    @Transactional
    public void generate() {

        if (packageRepository.count() > 0) {
            return;
        }

        for (long propertyCode = 1; propertyCode <= 20; propertyCode++) {

            long f1 = (propertyCode - 1) * 5 + 1;
            long f2 = f1 + 1;
            long f3 = f1 + 2;
            long f4 = f1 + 3;
            long f5 = f1 + 4;

            createPackage(propertyCode,
                    "베이직 패키지",
                    "부대시설 1 포함",
                    "30000",
                    new long[]{f1},
                    new int[]{1}
            );

            createPackage(propertyCode,
                    "레저 패키지",
                    "부대시설 2 포함",
                    "45000",
                    new long[]{f2},
                    new int[]{1}
            );

            createPackage(propertyCode,
                    "웰니스 패키지",
                    "부대시설 1 + 3",
                    "60000",
                    new long[]{f1, f3},
                    new int[]{1, 1}
            );

            createPackage(propertyCode,
                    "프리미엄 패키지",
                    "부대시설 2 + 4",
                    "75000",
                    new long[]{f2, f4},
                    new int[]{1, 1}
            );

            createPackage(propertyCode,
                    "올인원 패키지",
                    "부대시설 1 + 2 + 5",
                    "100000",
                    new long[]{f1, f2, f5},
                    new int[]{1, 1, 1}
            );
        }
    }

    private void createPackage(
            Long propertyCode,
            String name,
            String content,
            String price,
            long[] facilityCodes,
            int[] quantities
    ) {

        ReservationPackage reservationPackage =
                packageRepository.save(
                        ReservationPackage.createReservationPackage(
                                name,
                                content,
                                new BigDecimal(price),
                                propertyCode
                        )
                );

        for (int i = 0; i < facilityCodes.length; i++) {
            packageFacilityRepository.save(
                    PackageFacility.createPackageFacility(
                            reservationPackage.getPackageCode(),
                            facilityCodes[i],
                            quantities[i],
                            BigDecimal.ZERO // 패키지 포함이므로 0
                    )
            );
        }
    }
}
