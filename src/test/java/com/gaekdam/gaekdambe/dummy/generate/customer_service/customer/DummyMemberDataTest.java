package com.gaekdam.gaekdambe.dummy.generate.customer_service.customer;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.entity.Member;
import com.gaekdam.gaekdambe.customer_service.customer.command.infrastructure.repository.MemberRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class DummyMemberDataTest {

    private static final int BATCH = 500;

    @Autowired
    MemberRepository memberRepository;

    @PersistenceContext
    EntityManager em;

    @Transactional
    public void generate() {

        // membership 있는 고객 중 member 없는 고객만 뽑아서 생성
        @SuppressWarnings("unchecked")
        List<Object> rows = em.createNativeQuery("""
                select m.customer_code
                  from membership m
             left join member mb
                    on mb.customer_code = m.customer_code
                 where mb.customer_code is null
                """).getResultList();

        if (rows == null || rows.isEmpty()) return;

        LocalDateTime now = LocalDateTime.now();

        List<Member> buffer = new ArrayList<>(BATCH);

        for (Object o : rows) {
            long customerCode = ((Number) o).longValue();

            buffer.add(Member.registerMember(customerCode, now));

            if (buffer.size() == BATCH) {
                memberRepository.saveAll(buffer);
                em.flush();
                em.clear();
                buffer.clear();
            }
        }

        if (!buffer.isEmpty()) {
            memberRepository.saveAll(buffer);
            em.flush();
            em.clear();
        }
    }
}
