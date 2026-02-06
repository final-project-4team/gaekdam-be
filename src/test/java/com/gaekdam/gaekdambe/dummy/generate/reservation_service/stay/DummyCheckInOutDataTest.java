package com.gaekdam.gaekdambe.dummy.generate.reservation_service.stay;

import com.gaekdam.gaekdambe.reservation_service.stay.command.domain.entity.CheckInOut;
import com.gaekdam.gaekdambe.reservation_service.stay.command.domain.entity.Stay;
import com.gaekdam.gaekdambe.reservation_service.stay.command.domain.enums.CheckInOutChannel;
import com.gaekdam.gaekdambe.reservation_service.stay.command.domain.enums.CheckInOutRecordType;
import com.gaekdam.gaekdambe.reservation_service.stay.command.domain.enums.SettlementYn;
import com.gaekdam.gaekdambe.reservation_service.stay.command.infrastructure.repository.CheckInOutRepository;
import com.gaekdam.gaekdambe.reservation_service.stay.command.infrastructure.repository.StayRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
@Component
public class DummyCheckInOutDataTest {

    private static final int BATCH = 500;

    @Autowired StayRepository stayRepository;
    @Autowired CheckInOutRepository checkInOutRepository;
    @Autowired
    EntityManager em;

    @Transactional
    public void generate() {

        // [가드] 중복 생성 방지
        if (checkInOutRepository.count() > 0) return;

        Random random = new Random();
        CheckInOutChannel[] channels = CheckInOutChannel.values();

        List<CheckInOut> buffer = new ArrayList<>(BATCH);

        // [대상] stay가 있어야 checkin/out 생성 가능
        for (Stay stay : stayRepository.findAll()) {

            // [실제 체크인한 투숙만] 체크인 시간이 없으면 스킵
            if (stay.getActualCheckinAt() == null) continue;

            // [체크인] 무조건 1건 생성
            buffer.add(
                    CheckInOut.createCheckInOut(
                            CheckInOutRecordType.CHECK_IN,
                            stay.getActualCheckinAt(),
                            stay.getGuestCount(),
                            channels[random.nextInt(channels.length)],
                            SettlementYn.N,
                            stay.getStayCode()
                    )
            );

            // [체크아웃] completed(= actualCheckoutAt 존재)인 경우만 1건 생성
            if (stay.getActualCheckoutAt() != null) {
                buffer.add(
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

            // [배치 저장]
            if (buffer.size() >= BATCH) {
                checkInOutRepository.saveAll(buffer);
                em.flush();
                em.clear();
                buffer.clear();
            }
        }

        // [마지막 찌꺼기]
        if (!buffer.isEmpty()) {
            checkInOutRepository.saveAll(buffer);
            em.flush();
            em.clear();
        }
    }
}
