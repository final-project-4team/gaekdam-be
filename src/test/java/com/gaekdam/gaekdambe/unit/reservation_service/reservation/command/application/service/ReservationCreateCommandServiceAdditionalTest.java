package com.gaekdam.gaekdambe.unit.reservation_service.reservation.command.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import com.gaekdam.gaekdambe.reservation_service.reservation.command.application.dto.request.ReservationCreateRequest;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.application.service.ReservationCreateCommandService;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.domain.entity.Reservation;
import com.gaekdam.gaekdambe.reservation_service.reservation.command.infrastructure.repository.ReservationRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

class ReservationCreateCommandServiceAdditionalTest {

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
    void create_handlesPackagePriceNull_and_publishesEvent() {
        ReservationCreateRequest req = org.mockito.Mockito.mock(ReservationCreateRequest.class);
        when(req.getCheckinDate()).thenReturn(java.time.LocalDate.now());
        when(req.getCheckoutDate()).thenReturn(java.time.LocalDate.now().plusDays(1));
        when(req.getGuestCount()).thenReturn(1);
        when(req.getGuestType()).thenReturn(null);
        when(req.getReservationChannel()).thenReturn(null);
        when(req.getReservationRoomPrice()).thenReturn(java.math.BigDecimal.valueOf(200));
        when(req.getReservationPackagePrice()).thenReturn(null);
        when(req.getPropertyCode()).thenReturn(2L);
        when(req.getRoomCode()).thenReturn(3L);
        when(req.getCustomerCode()).thenReturn(4L);
        when(req.getPackageCode()).thenReturn(null);

        when(stageResolver.resolveStageCode("RESERVATION_CONFIRMED")).thenReturn(111L);

        when(reservationRepository.save(org.mockito.ArgumentMatchers.any(Reservation.class))).thenAnswer(invocation -> {
            Reservation r = invocation.getArgument(0);
            try {
                java.lang.reflect.Field f = Reservation.class.getDeclaredField("reservationCode");
                f.setAccessible(true);
                f.set(r, 777L);
            } catch (Exception e) {}
            return r;
        });

        Long code = service.create(req);
        assertThat(code).isEqualTo(777L);
        verify(reservationRepository).save(org.mockito.ArgumentMatchers.any(Reservation.class));
        verify(eventPublisher).publishEvent(org.mockito.ArgumentMatchers.any(com.gaekdam.gaekdambe.communication_service.messaging.command.domain.event.MessageJourneyEvent.class));
    }
}
