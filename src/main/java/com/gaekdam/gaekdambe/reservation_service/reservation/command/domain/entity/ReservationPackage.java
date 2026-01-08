package com.gaekdam.gaekdambe.reservation_service.reservation.command.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "reservation_pakage")
public class ReservationPackage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "package_code" , nullable = false)
    private Long packageCode;

    @Column(name = "package_name" , nullable = false , length = 50)
    private String packageName;

    @Column(name = "package_content" , nullable = false)
    private String packageContent;

    @Column(name = "facility_code")
    private Long facilityCode;

    // 추후 엔티티 생성 다 되면 fk 컬럼으로 변경
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "facility_code", nullable = false)
//    private Facility facility;

}
