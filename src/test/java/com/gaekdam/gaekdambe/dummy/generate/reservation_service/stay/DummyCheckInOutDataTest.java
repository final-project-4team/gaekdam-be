package com.gaekdam.gaekdambe.dummy.generate.reservation_service.stay;

import com.gaekdam.gaekdambe.reservation_service.stay.command.domain.entity.CheckInOut;
import com.gaekdam.gaekdambe.reservation_service.stay.command.domain.entity.Stay;
import com.gaekdam.gaekdambe.reservation_service.stay.command.infrastructure.repository.CheckInOutRepository;
import com.gaekdam.gaekdambe.reservation_service.stay.command.infrastructure.repository.StayRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Component
public class DummyCheckInOutDataTest {


    @Autowired
    private StayRepository stayRepository;

    @Autowired
    private CheckInOutRepository checkInOutRepository;

    @Transactional
    public void generate() {

        if (checkInOutRepository.count() > 0) {
            return;
        }

        Random random = new Random();
        String[] channels = {"FRONT", "KIOSK", "MOBILE"};

        List<Stay> stays = stayRepository.findAll();

        for (Stay stay : stays) {

            // CHECK_IN
            checkInOutRepository.save(
                    CheckInOut.createCheckInOut(
                            "CHECK_IN",
                            stay.getActualCheckinAt(),
                            stay.getGuestCount(),
                            channels[random.nextInt(channels.length)],
                            "N",
                            stay.getStayCode()
                    )
            );

            // CHECK_OUT (완료된 투숙만)
            if ("COMPLETED".equals(stay.getStayStatus())) {
                checkInOutRepository.save(
                        CheckInOut.createCheckInOut(
                                "CHECK_OUT",
                                stay.getActualCheckoutAt(),
                                stay.getGuestCount(),
                                channels[random.nextInt(channels.length)],
                                "Y",
                                stay.getStayCode()
                        )
                );
            }
        }
    }
}
