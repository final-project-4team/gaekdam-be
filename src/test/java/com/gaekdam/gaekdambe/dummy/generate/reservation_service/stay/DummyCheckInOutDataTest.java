package com.gaekdam.gaekdambe.dummy.generate.reservation_service.stay;

import com.gaekdam.gaekdambe.reservation_service.stay.command.domain.entity.CheckInOut;
import com.gaekdam.gaekdambe.reservation_service.stay.command.domain.entity.Stay;
import com.gaekdam.gaekdambe.reservation_service.stay.command.domain.enums.CheckInOutChannel;
import com.gaekdam.gaekdambe.reservation_service.stay.command.domain.enums.CheckInOutRecordType;
import com.gaekdam.gaekdambe.reservation_service.stay.command.domain.enums.SettlementYn;
import com.gaekdam.gaekdambe.reservation_service.stay.command.infrastructure.repository.CheckInOutRepository;
import com.gaekdam.gaekdambe.reservation_service.stay.command.infrastructure.repository.StayRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Random;
@Component
public class DummyCheckInOutDataTest {

    @Autowired
    StayRepository stayRepository;
    @Autowired
    CheckInOutRepository checkInOutRepository;

    @Transactional
    public void generate() {

        if (checkInOutRepository.count() > 0) return;

        Random random = new Random();
        CheckInOutChannel[] channels = CheckInOutChannel.values();

        for (Stay stay : stayRepository.findAll()) {

            //  실제 체크인한 투숙만
            if (stay.getActualCheckinAt() == null) continue;

            // CHECK_IN (무조건 1건)
            checkInOutRepository.save(
                    CheckInOut.createCheckInOut(
                            CheckInOutRecordType.CHECK_IN,
                            stay.getActualCheckinAt(),
                            stay.getGuestCount(),
                            channels[random.nextInt(channels.length)],
                            SettlementYn.N,
                            stay.getStayCode()
                    )
            );

            // CHECK_OUT (완료된 투숙만)
            if (stay.getActualCheckoutAt() != null) {
                checkInOutRepository.save(
                        CheckInOut.createCheckInOut(
                                CheckInOutRecordType.CHECK_OUT,
                                stay.getActualCheckoutAt(),
                                stay.getGuestCount(),
                                channels[random.nextInt(channels.length)],
                                SettlementYn.Y,
                                stay.getStayCode()
                        )
                );
            }
        }
    }
}