package com.gaekdam.gaekdambe.communication_service.messaging.bootstrap;

import com.gaekdam.gaekdambe.communication_service.messaging.command.domain.service.MessagingSeedFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MessagingSeedBootstrap {

    private final MessageJourneyStageSeed stageSeed;
    private final MessagingSeedFacade seedFacade;
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    public void seedAll() {

        // Stage 먼저 보장
        stageSeed.seed();

        // hotel_group별 seed
        List<Long> hotelGroupCodes =
                jdbcTemplate.queryForList(
                        "SELECT DISTINCT hotel_group_code FROM property",
                        Long.class
                );

        for (Long hotelGroupCode : hotelGroupCodes) {
            seedFacade.seedForHotelGroup(hotelGroupCode);
        }
    }
}