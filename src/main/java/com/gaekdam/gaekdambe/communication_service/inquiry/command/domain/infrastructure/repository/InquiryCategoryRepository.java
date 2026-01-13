package com.gaekdam.gaekdambe.communication_service.inquiry.command.domain.infrastructure.repository;

import com.gaekdam.gaekdambe.communication_service.inquiry.command.domain.entity.InquiryCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryCategoryRepository extends JpaRepository<InquiryCategory,Long> {
}
