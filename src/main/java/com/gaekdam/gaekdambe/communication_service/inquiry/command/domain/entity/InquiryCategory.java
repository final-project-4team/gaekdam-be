package com.gaekdam.gaekdambe.communication_service.inquiry.command.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "inquiry_category")
public class InquiryCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inquiry_category_code", nullable = false)
    private Long inquiryCategoryCode;

    @Column(name = "inquiry_category_name", nullable = false, length = 50)
    private String inquiryCategoryName;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    // 생성 메서드
    public static InquiryCategory create(
            String inquiryCategoryName,
            boolean isActive
    ) {
        return InquiryCategory.builder()
                .inquiryCategoryName(inquiryCategoryName)
                .isActive(isActive)
                .build();
    }

}