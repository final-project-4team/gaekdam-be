package com.gaekdam.gaekdambe.dummy.generate.reservation_service.stay;

import com.gaekdam.gaekdambe.reservation_service.reservation.command.domain.entity.Reservation;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.domain.enums.ReservationStatus;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.infrastructure.repository.ReservationRepository;
import com.gaekdam.gaekdambe.reservation_service.stay.command.domain.entity.Stay;
import com.gaekdam.gaekdambe.reservation_service.stay.command.domain.enums.StayStatus;
import com.gaekdam.gaekdambe.reservation_service.stay.command.infrastructure.repository.StayRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class DummyStayDataTest {

    private static final int BATCH = 500;

    @Autowired ReservationRepository reservationRepository;
    @Autowired StayRepository stayRepository;
    @Autowired
    EntityManager em;

    @Transactional
    public void generate() {

        // [가드] 이미 있으면 중복 생성 방지
        if (stayRepository.count() > 0) return;

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        Random random = new Random();

        List<Stay> buffer = new ArrayList<>(BATCH);

        // [대상] RESERVED 예약만 stay 후보 (취소/노쇼는 stay 생성 X)
        for (Reservation r :
                reservationRepository.findByReservationStatus(ReservationStatus.RESERVED)) {

            // [미래 예약] 체크인일이 오늘 이후면 아직 투숙 시작 전 → stay 생성 안 함
            if (r.getCheckinDate().isAfter(today)) continue;

            // 오늘 체크인 예약 → 70%는 stay 생성 X (체크인 예정)
            if (r.getCheckinDate().isEqual(today)) {
                if (random.nextDouble() < 0.7) {
                    continue;
                }
            }


            // [체크인/아웃] 체크인은 15시 고정, 체크아웃은 과거면 10시로 생성
            LocalDateTime checkinAt = r.getCheckinDate().atTime(15, 0);

            LocalDateTime checkoutAt = null;
            StayStatus status = StayStatus.STAYING;

            // [완료 투숙] 체크아웃일이 과거면 completed 처리
            if (r.getCheckoutDate().isBefore(today)) {
                checkoutAt = r.getCheckoutDate().atTime(10, 0);
                status = StayStatus.COMPLETED;
            }

            buffer.add(
                    Stay.createStay(
                            r.getReservationCode(),
                            r.getRoomCode(),
                            r.getCustomerCode(),
                            r.getGuestCount(),
                            checkinAt,
                            checkoutAt,
                            status
                    )
            );

            // [배치 저장] 메모리/속도 개선
            if (buffer.size() == BATCH) {
                stayRepository.saveAll(buffer);
                em.flush();
                em.clear();
                buffer.clear();
            }
        }

        // [마지막 찌꺼기]
        if (!buffer.isEmpty()) {
            stayRepository.saveAll(buffer);
            em.flush();
            em.clear();
        }
    }
}
