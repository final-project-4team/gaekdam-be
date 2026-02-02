package com.gaekdam.gaekdambe.reservation_service.reservation.query.service;

import com.gaekdam.gaekdambe.global.crypto.DecryptionService;

import com.gaekdam.gaekdambe.iam_service.log.command.application.aop.annotation.LogPersonalInfo;
import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.seeds.PermissionTypeKey;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.detail.*;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.mapper.ReservationDetailMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReservationDetailQueryService {

        private final ReservationDetailMapper mapper;
        private final DecryptionService decryptionService;

        @LogPersonalInfo(type = PermissionTypeKey.CUSTOMER_READ, purpose = "고객 정보 조회")
        public ReservationDetailResponse getReservationDetail(Long reservationCode, String reason) {

                CustomerCryptoRow row = mapper.findCustomerCrypto(reservationCode);

                if (row == null) {
                        throw new IllegalArgumentException("Customer crypto row not found for reservationCode: " + reservationCode);
                }

                String decryptedName = decryptionService.decrypt(
                                row.getCustomerCode(),
                                row.getDekEnc(),
                                row.getCustomerNameEnc());
                // 멤버 여부
                boolean isMember = mapper.existsMember(row.getCustomerCode());

                // 전화번호
                CustomerContactCryptoRow phoneRow = mapper.findPrimaryPhone(row.getCustomerCode());

                String phoneNumber = null;
                if (phoneRow != null) {
                        phoneNumber = decryptionService.decrypt(
                                        row.getCustomerCode(),
                                        row.getDekEnc(),
                                        phoneRow.getContactValueEnc());
                }

                CustomerInfo customer = CustomerInfo.builder()
                                .customerCode(row.getCustomerCode())
                                .customerName(decryptedName)
                                .nationalityType(row.getNationalityType())
                                .contractType(row.getContractType())
                                .customerStatus(row.getCustomerStatus())
                                .isMember(isMember)
                                .phoneNumber(phoneNumber)
                                .build();

                PackageInfo packageInfo = mapper.findPackageInfo(reservationCode);
                if (packageInfo != null) {
                        packageInfo = PackageInfo.builder()
                                        .packageName(packageInfo.getPackageName())
                                        .packageContent(packageInfo.getPackageContent())
                                        .packagePrice(packageInfo.getPackagePrice())
                                        .facilities(mapper.findPackageFacilities(reservationCode))
                                        .build();
                }

                return ReservationDetailResponse.builder()
                                .reservation(mapper.findReservationInfo(reservationCode))
                                .customer(customer)
                                .room(mapper.findRoomInfo(reservationCode))
                                .stay(mapper.findStayInfo(reservationCode))
                                .checkInOut(mapper.findCheckInOutInfo(reservationCode))
                                .facilityUsages(mapper.findFacilityUsageSummary(reservationCode))
                                .packageInfo(packageInfo)
                                .build();
        }
}
