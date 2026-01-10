package com.gaekdam.gaekdambe.customer_service.customer.command.domain.entity;

import com.gaekdam.gaekdambe.customer_service.customer.command.domain.ContractType;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.CustomerStatus;
import com.gaekdam.gaekdambe.customer_service.customer.command.domain.CustomerType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "customer")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_code", nullable = false)
    private Long customerCode;

    @Column(name = "hotel_group_code", nullable = false)
    private Long hotelGroupCode;

    @Column(name = "customer_name_enc")
    private byte[] customerNameEnc;

    @Column(name = "customer_name_hash")
    private String customerNameHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "nationality_type", nullable = false, length = 30)
    private NationalityType nationalityType;

    @Enumerated(EnumType.STRING)
    @Column(name = "contract_type", nullable = false, length = 30)
    private ContractType contractType;

    @Enumerated(EnumType.STRING)
    @Column(name = "customer_type", nullable = false, length = 50)
    private CustomerType customerType;

    @Enumerated(EnumType.STRING)
    @Column(name = "customer_status", nullable = false, length = 30)
    private CustomerStatus customerStatus;

    @Column(name = "caution_at")
    private LocalDateTime cautionAt;

    @Column(name = "inactive_at")
    private LocalDateTime inactiveAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "kms_key_id", nullable = false, length = 100)
    private String kmsKeyId;

    @Column(name = "dek_enc", nullable = false, columnDefinition = "VARBINARY(512)")
    private byte[] dekEnc;

    private Customer(
            Long hotelGroupCode,
            byte[] customerNameEnc,
            String customerNameHash,
            NationalityType nationalityType,
            ContractType contractType,
            CustomerType customerType,
            String kmsKeyId,
            byte[] dekEnc,
            LocalDateTime now
    ) {
        this.hotelGroupCode = hotelGroupCode;
        this.customerNameEnc = customerNameEnc;
        this.customerNameHash = customerNameHash;
        this.nationalityType = nationalityType;
        this.contractType = contractType;
        this.customerType = customerType;
        this.customerStatus = CustomerStatus.ACTIVE;
        this.kmsKeyId = kmsKeyId;
        this.dekEnc = dekEnc;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public static Customer createCustomer(
            Long hotelGroupCode,
            byte[] customerNameEnc,
            String customerNameHash,
            NationalityType nationalityType,
            ContractType contractType,
            CustomerType customerType,
            String kmsKeyId,
            byte[] dekEnc,
            LocalDateTime now
    ) {
        return new Customer(
                hotelGroupCode,
                customerNameEnc,
                customerNameHash,
                nationalityType,
                contractType,
                customerType,
                kmsKeyId,
                dekEnc,
                now
        );
    }

    public void changeCustomerStatus(CustomerStatus afterStatus, LocalDateTime now) {
        if (afterStatus == null) {
            throw new IllegalArgumentException("afterStatus must not be null");
        }
        this.customerStatus = afterStatus;
        this.updatedAt = now;

        if (afterStatus == CustomerStatus.CAUTION) {
            this.cautionAt = now;
        }
        if (afterStatus == CustomerStatus.INACTIVE) {
            this.inactiveAt = now;
        }
    }
}
