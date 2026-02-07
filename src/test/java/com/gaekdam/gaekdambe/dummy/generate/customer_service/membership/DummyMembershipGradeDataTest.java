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
import java.util.Optional;

@Component
public class DummyMembershipGradeDataTest {

    @Autowired MembershipGradeRepository membershipGradeRepository;
    @Autowired HotelGroupRepository hotelGroupRepository;

    @PersistenceContext EntityManager em;

    @Transactional
    public void generate() {

        List<HotelGroup> hotelGroups = hotelGroupRepository.findAll();
        if (hotelGroups.isEmpty()) return;

        for (HotelGroup hg : hotelGroups) {
            upsert(hg, "BASIC",  1L, "Basic membership grade", 0L,         0);
            upsert(hg, "BRONZE", 2L, "Bronze membership grade", 200_000L,  0);
            upsert(hg, "SILVER", 3L, "Silver membership grade", 500_000L,  0);
            upsert(hg, "GOLD",   4L, "Gold membership grade", 1_000_000L,  0);
            upsert(hg, "VIP",    5L, "VIP membership grade", 1_500_000L,  0);
        }

        em.flush();
        em.clear();
    }

    private void upsert(
            HotelGroup hg,
            String gradeName,
            Long tierLevel,
            String tierComment,
            Long calculationAmount,
            Integer calculationCount
    ) {
        Optional<MembershipGrade> opt =
                membershipGradeRepository.findByHotelGroup_HotelGroupCodeAndGradeName(
                        hg.getHotelGroupCode(), gradeName
                );

        if (opt.isPresent()) {
            MembershipGrade existing = opt.get();
            existing.update(
                    gradeName,
                    tierLevel,
                    tierComment,
                    calculationAmount,
                    calculationCount
            );
            // save() 없어도 Dirty Checking으로 업데이트됨
        } else {
            membershipGradeRepository.save(
                    MembershipGrade.registerMembershipGrade(
                            hg,
                            gradeName,
                            tierLevel,
                            tierComment,
                            calculationAmount,
                            calculationCount
                    )
            );
        }
    }
}
