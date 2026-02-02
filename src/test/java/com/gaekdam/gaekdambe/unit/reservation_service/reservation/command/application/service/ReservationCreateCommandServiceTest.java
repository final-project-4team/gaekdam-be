package com.gaekdam.gaekdambe.unit.reservation_service.reservation.command.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import com.gaekdam.gaekdambe.reservation_service.reservation.command.application.dto.request.ReservationCreateRequest;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.application.service.ReservationCreateCommandService;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.domain.entity.Reservation;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.infrastructure.repository.ReservationRepository;

class ReservationCreateCommandServiceTest {

    @Mock
    ReservationRepository reservationRepository;
    @Mock
    ApplicationEventPublisher eventPublisher;
    @Mock
    com.gaekdam.gaekdambe.communication_service.messaging.command.domain.resolver.MessageStageResolver stageResolver;

    private ReservationCreateCommandService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new ReservationCreateCommandService(reservationRepository, eventPublisher, stageResolver);
    }

    @Test
    void create_savesReservation_and_publishesEvent() {
        // DTO는 lombok @Getter만 사용하므로, 테스트에서는 Mockito mock으로 getter 동작을 스텁합니다.
        ReservationCreateRequest req = org.mockito.Mockito.mock(ReservationCreateRequest.class);
        org.mockito.Mockito.when(req.getCheckinDate()).thenReturn(java.time.LocalDate.now());
        org.mockito.Mockito.when(req.getCheckoutDate()).thenReturn(java.time.LocalDate.now().plusDays(1));
        org.mockito.Mockito.when(req.getGuestCount()).thenReturn(2);
        org.mockito.Mockito.when(req.getGuestType()).thenReturn(null);
        org.mockito.Mockito.when(req.getReservationChannel()).thenReturn(null);
        org.mockito.Mockito.when(req.getReservationRoomPrice()).thenReturn(java.math.BigDecimal.valueOf(100));
        org.mockito.Mockito.when(req.getReservationPackagePrice()).thenReturn(java.math.BigDecimal.ZERO);
        org.mockito.Mockito.when(req.getPropertyCode()).thenReturn(1L);
        org.mockito.Mockito.when(req.getRoomCode()).thenReturn(1L);
        org.mockito.Mockito.when(req.getCustomerCode()).thenReturn(10L);
        org.mockito.Mockito.when(req.getPackageCode()).thenReturn(null);

        when(stageResolver.resolveStageCode("RESERVATION_CONFIRMED")).thenReturn(999L);

        // 저장 시 엔티티의 reservationCode가 부여되는 시뮬레이션: reflection으로 필드에 값 설정
        org.mockito.Mockito.when(reservationRepository.save(org.mockito.ArgumentMatchers.any(Reservation.class)))
            .thenAnswer(invocation -> {
                Reservation rr = invocation.getArgument(0);
                try {
                    java.lang.reflect.Field f = Reservation.class.getDeclaredField("reservationCode");
                    f.setAccessible(true);
                    f.set(rr, 123L);
                } catch (Exception e) {
                    // ignore
                }
                return rr;
            });

        Long resCode = service.create(req);
        // 생성된 reservationCode가 reflection 모킹으로 채워진 것을 확인
        assertThat(resCode).isEqualTo(123L);

        verify(reservationRepository).save(org.mockito.ArgumentMatchers.any(Reservation.class));
        // ApplicationEventPublisher에는 오버로드된 publishEvent 메서드가 있어 any()로는 모호할 수 있으므로,
        // 실제로 발행되는 MessageJourneyEvent 타입을 명시적으로 매처로 사용합니다.
        verify(eventPublisher).publishEvent(org.mockito.ArgumentMatchers.any(com.gaekdam.gaekdambe.communication_service.messaging.command.domain.event.MessageJourneyEvent.class));
    }
}
