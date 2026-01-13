package com.gaekdam.gaekdambe.dummy.generate.communication_service.inquiry;

import com.gaekdam.gaekdambe.communication_service.inquiry.command.domain.entity.InquiryCategory;

import com.gaekdam.gaekdambe.communication_service.inquiry.command.domain.infrastructure.repository.InquiryCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class DummyInquiryCategoryDataTest {

    @Autowired
    private InquiryCategoryRepository inquiryCategoryRepository;

    public void generate() {

        // 이미 있으면 스킵
        if (inquiryCategoryRepository.count() > 0) {
            return;
        }

        inquiryCategoryRepository.save(InquiryCategory.create("시설", true));
        inquiryCategoryRepository.save(InquiryCategory.create("결제", true));
        inquiryCategoryRepository.save(InquiryCategory.create("고객", true));
        inquiryCategoryRepository.save(InquiryCategory.create("직원", true));
        inquiryCategoryRepository.save(InquiryCategory.create("기타", true));
    }
}