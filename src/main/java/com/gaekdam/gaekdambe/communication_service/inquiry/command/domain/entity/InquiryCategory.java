package com.gaekdam.gaekdambe.communication_service.inquiry.command.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
}
