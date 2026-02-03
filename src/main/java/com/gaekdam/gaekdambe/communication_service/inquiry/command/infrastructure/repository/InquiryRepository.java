package com.gaekdam.gaekdambe.communication_service.inquiry.command.infrastructure.repository;

import com.gaekdam.gaekdambe.communication_service.inquiry.command.domain.entity.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryRepository extends JpaRepository<Inquiry,Long> {
}
