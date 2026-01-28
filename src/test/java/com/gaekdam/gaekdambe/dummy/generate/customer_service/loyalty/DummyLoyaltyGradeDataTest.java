package com.gaekdam.gaekdambe.dummy.generate.customer_service.loyalty;

import com.gaekdam.gaekdambe.customer_service.loyalty.command.domain.entity.LoyaltyGrade;
import com.gaekdam.gaekdambe.customer_service.loyalty.command.infrastructure.repository.LoyaltyGradeRepository;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.domain.entity.HotelGroup;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.infrastructure.repository.HotelGroupRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DummyLoyaltyGradeDataTest {

    @Autowired LoyaltyGradeRepository loyaltyGradeRepository;
    @Autowired HotelGroupRepository hotelGroupRepository;

    @PersistenceContext EntityManager em;

    @Transactional
    public void generate() {

        if (loyaltyGradeRepository.count() > 0) return;

        List<HotelGroup> hotelGroups = hotelGroupRepository.findAll();
        if (hotelGroups.isEmpty()) return;

        for (HotelGroup hg : hotelGroups) {

            // 기본 등급
            loyaltyGradeRepository.save(LoyaltyGrade.registerLoyaltyGrade(
                    hg,
                    "GENERAL",
                    1L,
                    "General loyalty grade",
                    0L,
                    0,
                    12,
                    1
            ));

            // 상위 등급
            loyaltyGradeRepository.save(LoyaltyGrade.registerLoyaltyGrade(
                    hg,
                    "EXCELLENT",
                    2L,
                    "Excellent loyalty grade",
                    0L,
                    10,
                    12,
                    1
            ));
        }

        em.flush();
        em.clear();
    }
}
