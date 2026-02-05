package com.gaekdam.gaekdambe.communication_service.inquiry.command.domain.entity;

import com.gaekdam.gaekdambe.communication_service.inquiry.command.domain.InquiryStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "inquiry", indexes = {
        @Index(name = "IDX_inquiry_property", columnList = "property_code"),
        @Index(name = "IDX_inquiry_status", columnList = "inquiry_status"),
        @Index(name = "IDX_inquiry_category", columnList = "inquiry_category_code"),
        @Index(name = "idx_inquiry_property_status", columnList = "property_code, inquiry_status, inquiry_code")
})
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inquiry_code", nullable = false)
    private Long inquiryCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "inquiry_status", nullable = false, length = 30)
    private InquiryStatus inquiryStatus;

    @Column(name = "inquiry_title", nullable = false, length = 255)
    private String inquiryTitle;

    @Lob
    @Column(name = "inquiry_content", nullable = false)
    private String inquiryContent;

    @Lob
    @Column(name = "answer_content")
    private String answerContent;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "customer_code", nullable = false)
    private Long customerCode;

    @Column(name = "employee_code")
    private Long employeeCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquiry_category_code", nullable = false)
    private InquiryCategory category;

    @Column(name = "property_code", nullable = false)
    private Long propertyCode;

    public static Inquiry create(
            Long propertyCode,
            Long customerCode,
            InquiryCategory category,
            String title,
            String content) {
        LocalDateTime now = LocalDateTime.now();
        return Inquiry.builder()
                .propertyCode(propertyCode)
                .customerCode(customerCode)
                .category(category)
                .inquiryTitle(title)
                .inquiryContent(content)
                .inquiryStatus(InquiryStatus.IN_PROGRESS)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public void assignManager(Long employeeCode) {
        this.employeeCode = employeeCode;
        this.updatedAt = LocalDateTime.now();
    }

    public void answer(String answerContent) {
        this.answerContent = answerContent;
        this.inquiryStatus = InquiryStatus.ANSWERED;
        this.updatedAt = LocalDateTime.now();
    }
}
