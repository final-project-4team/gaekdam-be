package com.gaekdam.gaekdambe.unit.reservation_service.stay.command.application.service;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.event.MessageJourneyEvent;
import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.resolver.MessageStageResolver;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.domain.entity.Reservation;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.infrastructure.repository.ReservationRepository;
import com.gaekdam.gaekdambe.reservation_service.stay.command.application.dto.request.CheckInRequest;
import com.gaekdam.gaekdambe.reservation_service.stay.command.application.dto.request.CheckOutRequest;
import com.gaekdam.gaekdambe.reservation_service.stay.command.application.service.CheckInOutCommandService;
import com.gaekdam.gaekdambe.reservation_service.stay.command.domain.entity.CheckInOut;
import com.gaekdam.gaekdambe.reservation_service.stay.command.domain.entity.Stay;
import com.gaekdam.gaekdambe.reservation_service.stay.command.infrastructure.repository.CheckInOutRepository;
import com.gaekdam.gaekdambe.reservation_service.stay.command.infrastructure.repository.StayRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CheckInOutCommandServiceTest {

    @Mock
    CheckInOutRepository checkInOutRepository;
    @Mock
    StayRepository stayRepository;
    @Mock
    ReservationRepository reservationRepository;
    @Mock
    ApplicationEventPublisher eventPublisher;
    @Mock
    MessageStageResolver stageResolver;

    private CheckInOutCommandService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new CheckInOutCommandService(checkInOutRepository, stayRepository, reservationRepository, eventPublisher, stageResolver);
    }

    @Test
    void 체크인_정상흐름_체크인기록저장_이벤트발행() {
        // given: 예약이 존재하고, 이미 체크인된 Stay는 없음
        Reservation reservation = Reservation.builder()
                .reservationCode(100L)
                .roomCode(10L)
                .customerCode(20L)
                .propertyCode(1L)
                .checkinDate(LocalDate.now())
                .checkoutDate(LocalDate.now().plusDays(1))
                .guestCount(2)
                .guestType(null)
                .reservationChannel(null)
                .reservationRoomPrice(BigDecimal.valueOf(100))
                .reservationPackagePrice(BigDecimal.ZERO)
                .totalPrice(BigDecimal.valueOf(100))
                .reservedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        when(reservationRepository.findById(100L)).thenReturn(Optional.of(reservation));
        when(stayRepository.findByReservationCode(100L)).thenReturn(Optional.empty());
        when(stageResolver.resolveStageCode("CHECKIN_CONFIRMED")).thenReturn(777L);

        // stay 저장 시 DB가 PK를 부여하는 시뮬레이션: save 호출시 stayCode 세팅
        when(stayRepository.save(any(Stay.class))).thenAnswer(invocation -> {
            Stay s = invocation.getArgument(0);
            try {
                java.lang.reflect.Field f = Stay.class.getDeclaredField("stayCode");
                f.setAccessible(true);
                f.set(s, 555L);
            } catch (Exception e) {
                // ignore
            }
            return s;
        });

        // DTO는 lombok @Getter만 있으므로 mock으로 getter를 stub
        CheckInRequest req = mock(CheckInRequest.class);
        when(req.getReservationCode()).thenReturn(100L);
        when(req.getGuestCount()).thenReturn(2);
        when(req.getRecordChannel()).thenReturn(null);
        when(req.getSettlementYn()).thenReturn(null);

        // when
        service.checkIn(req);

        // then: 체크인 기록과 Stay 저장, 이벤트 발행 확인
        verify(stayRepository).save(any(Stay.class));
        verify(checkInOutRepository).save(any(CheckInOut.class));

        ArgumentCaptor<MessageJourneyEvent> captor = ArgumentCaptor.forClass(MessageJourneyEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        MessageJourneyEvent ev = captor.getValue();
        assertThat(ev.getStageCode()).isEqualTo(777L);
        assertThat(ev.getReservationCode()).isEqualTo(100L);
        assertThat(ev.getStayCode()).isEqualTo(555L);
    }

    @Test
    void 체크인_예약없음_예외() {
        CheckInRequest req = mock(CheckInRequest.class);
        when(req.getReservationCode()).thenReturn(999L);
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.checkIn(req));
    }

    @Test
    void 체크인_이미체크인된예약_예외() {
        Reservation reservation = Reservation.builder().reservationCode(200L).roomCode(11L).customerCode(22L).propertyCode(1L).checkinDate(LocalDate.now()).checkoutDate(LocalDate.now().plusDays(1)).guestCount(1).reservationRoomPrice(BigDecimal.ONE).totalPrice(BigDecimal.ONE).reservedAt(LocalDateTime.now()).createdAt(LocalDateTime.now()).build();
        when(reservationRepository.findById(200L)).thenReturn(Optional.of(reservation));
        // 이미 stay 존재
        Stay existing = Stay.builder().stayCode(300L).reservationCode(200L).guestCount(1).stayStatus(null).createdAt(LocalDateTime.now()).build();
        when(stayRepository.findByReservationCode(200L)).thenReturn(Optional.of(existing));

        CheckInRequest req = mock(CheckInRequest.class);
        when(req.getReservationCode()).thenReturn(200L);

        assertThrows(IllegalStateException.class, () -> service.checkIn(req));
    }

    @Test
    void 체크아웃_정상흐름_stay종료_기록저장_이벤트발행() {
        // given: Stay 존재, 아직 체크아웃되지 않음
        Stay stay = Stay.builder()
                .stayCode(400L)
                .reservationCode(123L)
                .roomCode(10L)
                .customerCode(20L)
                .guestCount(2)
                .stayStatus(null)
                .createdAt(LocalDateTime.now())
                .build();

        when(stayRepository.findById(400L)).thenReturn(Optional.of(stay));
        when(stageResolver.resolveStageCode("CHECKOUT_CONFIRMED")).thenReturn(888L);

        CheckOutRequest req = mock(CheckOutRequest.class);
        when(req.getStayCode()).thenReturn(400L);
        when(req.getRecordChannel()).thenReturn(null);
        when(req.getSettlementYn()).thenReturn(null);

        // when
        service.checkOut(req);

        // then
        verify(checkInOutRepository).save(any(CheckInOut.class));
        ArgumentCaptor<MessageJourneyEvent> captor = ArgumentCaptor.forClass(MessageJourneyEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        MessageJourneyEvent ev = captor.getValue();
        assertThat(ev.getStageCode()).isEqualTo(888L);
        assertThat(ev.getReservationCode()).isNull();
        assertThat(ev.getStayCode()).isEqualTo(400L);

        // stay에 actualCheckoutAt이 세팅되었는지 확인
        assertThat(stay.getActualCheckoutAt()).isNotNull();
    }

    @Test
    void 체크아웃_stay없음_예외() {
        CheckOutRequest req = mock(CheckOutRequest.class);
        when(req.getStayCode()).thenReturn(9999L);
        when(stayRepository.findById(9999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.checkOut(req));
    }

    @Test
    void 체크아웃_이미체크아웃된_stay_예외() {
        Stay stay = Stay.builder()
                .stayCode(500L)
                .reservationCode(321L)
                .actualCheckoutAt(LocalDateTime.now().minusDays(1))
                .guestCount(1)
                .createdAt(LocalDateTime.now())
                .build();
        when(stayRepository.findById(500L)).thenReturn(Optional.of(stay));

        CheckOutRequest req = mock(CheckOutRequest.class);
        when(req.getStayCode()).thenReturn(500L);

        assertThrows(IllegalStateException.class, () -> service.checkOut(req));
    }
}
