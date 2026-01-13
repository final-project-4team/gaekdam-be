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

        // 이미 데이터 있으면 스킵
        if (checkInOutRepository.count() > 0) {
            return;
        }

        Random random = new Random();
        CheckInOutChannel[] channels = CheckInOutChannel.values();

        List<Stay> stays = stayRepository.findAll();

        for (Stay stay : stays) {

            // CHECK_IN
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
            if ("COMPLETED".equals(stay.getStayStatus())) {
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
