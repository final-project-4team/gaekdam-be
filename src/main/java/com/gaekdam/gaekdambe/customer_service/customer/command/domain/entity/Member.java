package com.gaekdam.gaekdambe.customer_service.customer.command.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "member")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_code", nullable = false)
    private Long memberCode;

    @Column(name = "customer_code", nullable = false)
    private Long customerCode;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private Member(Long customerCode, LocalDateTime now) {
        this.customerCode = customerCode;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public static Member registerMember(Long customerCode, LocalDateTime now) {
        return new Member(customerCode, now);
    }

    public void touchUpdatedAt(LocalDateTime now) {
        this.updatedAt = now;
    }
}
