package com.gaekdam.gaekdambe.dummy.generate.communication_service.inquiry;

import com.gaekdam.gaekdambe.communication_service.inquiry.command.domain.entity.InquiryCategory;
import com.gaekdam.gaekdambe.communication_service.inquiry.command.infrastructure.repository.InquiryCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DummyInquiryCategoryDataTest {

    @Autowired
    private InquiryCategoryRepository inquiryCategoryRepository;

    @Transactional
    public void generate() {
        if (inquiryCategoryRepository.count() > 0) return;

        inquiryCategoryRepository.save(InquiryCategory.create("문의", true));
        inquiryCategoryRepository.save(InquiryCategory.create("클레임", true));
    }
}
