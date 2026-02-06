package com.gaekdam.gaekdambe.customer_service.customer.command.domain.entity;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.ContactType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "customer_contact")
public class CustomerContact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contact_code", nullable = false)
    private Long contactCode;

    @Column(name = "customer_code", nullable = false)
    private Long customerCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "contact_type", nullable = false, length = 20)
    private ContactType contactType;

    @Column(name = "contact_value_enc", nullable = false, columnDefinition = "VARBINARY(128)")
    private byte[] contactValueEnc;

    @Column(name = "contact_value_hash", nullable = false, length = 255)
    private String contactValueHash;

    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary;

    @Column(name = "marketing_opt_in", nullable = false)
    private Boolean marketingOptIn;

    @Column(name = "consent_at")
    private LocalDateTime consentAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private CustomerContact(
            Long customerCode,
            ContactType contactType,
            byte[] contactValueEnc,
            String contactValueHash,
            Boolean isPrimary,
            Boolean marketingOptIn,
            LocalDateTime consentAt,
            LocalDateTime now
    ) {
        this.customerCode = customerCode;
        this.contactType = contactType;
        this.contactValueEnc = contactValueEnc;
        this.contactValueHash = contactValueHash;
        this.isPrimary = isPrimary;
        this.marketingOptIn = marketingOptIn;
        this.consentAt = consentAt;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public static CustomerContact createCustomerContact(
            Long customerCode,
            ContactType contactType,
            byte[] contactValueEnc,
            String contactValueHash,
            Boolean isPrimary,
            Boolean marketingOptIn,
            LocalDateTime consentAt,
            LocalDateTime now
    ) {
        Boolean normalizedIsPrimary = (isPrimary != null) ? isPrimary : Boolean.FALSE;
        Boolean normalizedOptIn = (marketingOptIn != null) ? marketingOptIn : Boolean.FALSE;

        // opt-in false면 동의일시는 무조건 null
        // opt-in true인데 동의일시가 없으면 now로 보정
        LocalDateTime normalizedConsentAt =
                Boolean.TRUE.equals(normalizedOptIn)
                        ? (consentAt != null ? consentAt : now)
                        : null;

        return new CustomerContact(
                customerCode,
                contactType,
                contactValueEnc,
                contactValueHash,
                normalizedIsPrimary,
                normalizedOptIn,
                normalizedConsentAt,
                now
        );
    }

    public void markAsPrimaryContact(LocalDateTime now) {
        this.isPrimary = Boolean.TRUE;
        this.updatedAt = now;
    }
}
