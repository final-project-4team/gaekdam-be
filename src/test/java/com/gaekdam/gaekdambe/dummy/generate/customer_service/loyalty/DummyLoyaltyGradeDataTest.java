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

    @Autowired
    LoyaltyGradeRepository loyaltyGradeRepository;
    @Autowired
    HotelGroupRepository hotelGroupRepository;

    @PersistenceContext
    EntityManager em;

    @Transactional
    public void generate() {

        List<HotelGroup> hotelGroups = hotelGroupRepository.findAll();
        if (hotelGroups.isEmpty())
            return;

        for (HotelGroup hg : hotelGroups) {

            // 호텔그룹별로 존재여부 체크
            boolean hasGeneral = loyaltyGradeRepository.existsByHotelGroupAndLoyaltyGradeName(hg, "GENERAL");
            boolean hasExcellent = loyaltyGradeRepository.existsByHotelGroupAndLoyaltyGradeName(hg, "EXCELLENT");

            if (!hasGeneral) {
                loyaltyGradeRepository.save(LoyaltyGrade.registerLoyaltyGrade(
                        hg,
                        "GENERAL",
                        1L,
                        "General loyalty grade",
                        0L,
                        0
                ));
            }

            if (!hasExcellent) {
                loyaltyGradeRepository.save(LoyaltyGrade.registerLoyaltyGrade(
                        hg,
                        "EXCELLENT",
                        2L,
                        "Excellent loyalty grade",
                        0L,
                        10
                ));
            }
        }

        em.flush();
        em.clear();
    }
}
