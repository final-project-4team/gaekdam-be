package com.gaekdam.gaekdambe.dummy.generate.customer_service.membership;

import com.gaekdam.gaekdambe.customer_service.membership.command.domain.entity.MembershipGrade;
import com.gaekdam.gaekdambe.customer_service.membership.command.infrastructure.repository.MembershipGradeRepository;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.domain.entity.HotelGroup;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.infrastructure.repository.HotelGroupRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DummyMembershipGradeDataTest {

    @Autowired MembershipGradeRepository membershipGradeRepository;
    @Autowired HotelGroupRepository hotelGroupRepository;

    @PersistenceContext EntityManager em;

    @Transactional
    public void generate() {

        if (membershipGradeRepository.count() > 0) return;

        List<HotelGroup> hotelGroups = hotelGroupRepository.findAll();
        if (hotelGroups.isEmpty()) return;

        for (HotelGroup hg : hotelGroups) {

            membershipGradeRepository.save(MembershipGrade.registerMembershipGrade(
                    hg, "BASIC",  1L, "Basic membership grade",
                    0L, 0
            ));

            membershipGradeRepository.save(MembershipGrade.registerMembershipGrade(
                    hg, "BRONZE", 2L, "Bronze membership grade",
                    200_000L, 0
            ));

            membershipGradeRepository.save(MembershipGrade.registerMembershipGrade(
                    hg, "SILVER", 3L, "Silver membership grade",
                    500_000L, 0
            ));

            membershipGradeRepository.save(MembershipGrade.registerMembershipGrade(
                    hg, "GOLD",   4L, "Gold membership grade",
                    1_000_000L, 0
            ));

            membershipGradeRepository.save(MembershipGrade.registerMembershipGrade(
                    hg, "VIP",    5L, "VIP membership grade",
                    2_000_000L, 0
            ));
        }

        em.flush();
        em.clear();
    }
}
