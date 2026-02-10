package com.gaekdam.gaekdambe.reservation_service.reservation.query.controller;

import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gaekdam.gaekdambe.global.config.model.ApiResponse;
import com.gaekdam.gaekdambe.global.config.security.CustomUser;
import com.gaekdam.gaekdambe.global.paging.PageRequest;
import com.gaekdam.gaekdambe.global.paging.PageResponse;
import com.gaekdam.gaekdambe.global.paging.SortRequest;
import com.gaekdam.gaekdambe.iam_service.log.command.application.aop.annotation.AuditLog;
import com.gaekdam.gaekdambe.iam_service.permission_type.command.domain.seeds.PermissionTypeKey;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.request.OperationBoardSearchRequest;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.request.ReservationSearchRequest;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.OperationBoardResponse;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.dto.response.ReservationResponse;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.service.OperationBoardQueryService;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.service.ReservationQueryService;
import com.gaekdam.gaekdambe.reservation_service.reservation.query.service.TodayOperationQueryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "예약")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reservations")
public class ReservationQueryController {

        private final OperationBoardQueryService operationBoardQueryService;
        private final TodayOperationQueryService todayOperationQueryService;
        private final ReservationQueryService reservationQueryService;

        @GetMapping
        @PreAuthorize("hasAnyAuthority('RESERVATION_LIST','CUSTOMER_READ')")
        @AuditLog(details = "", type = PermissionTypeKey.RESERVATION_LIST)
        @Operation(summary = "예약 리스트 조회", description = "예약을 리스트 조회합니다.")
        public ApiResponse<PageResponse<ReservationResponse>> getReservations(
                        @Parameter(hidden = true) @AuthenticationPrincipal CustomUser customUser,
                        PageRequest page,
                        ReservationSearchRequest search,
                        SortRequest sort) {
                search.setHotelGroupCode(customUser.getHotelGroupCode());

                if (sort == null || sort.getSortBy() == null) {
                        sort = new SortRequest();
                        sort.setSortBy("created_at");
                        sort.setDirection("DESC");
                }

                return ApiResponse.success(
                                reservationQueryService.getReservations(page, search, sort));
        }

        // 통합 예약 조회 (리스트)
        @GetMapping("/operations")
        @PreAuthorize("hasAuthority('RESERVATION_LIST')")
        @Operation(summary = "운영 보드 리스트 조회", description = "운영 보드를 리스트 조회 합니다.")
        public ApiResponse<PageResponse<OperationBoardResponse>> getOperationBoard(
                        @Parameter(hidden = true) @AuthenticationPrincipal CustomUser customUser,
                        PageRequest page,
                        OperationBoardSearchRequest search,
                        SortRequest sort) {
                search.setHotelGroupCode(customUser.getHotelGroupCode());

                if (sort == null || sort.getSortBy() == null) {
                        sort = new SortRequest();
                        sort.setSortBy("r.reservation_code");
                        sort.setDirection("DESC");
                }

                return ApiResponse.success(
                                operationBoardQueryService.findOperationBoard(page, search, sort));
        }

        // 오늘의 예약정보 리스트(체크인예정 ,체크아웃예정, 투숙중)
        @GetMapping("/today/operations")
        @PreAuthorize("hasAuthority('TODAY_RESERVATION_LIST')")
        @AuditLog(details = "", type = PermissionTypeKey.TODAY_RESERVATION_LIST)
        @Operation(summary = "오늘의 예약 운영 현황 리스트 조회", description = "오늘의 체크인/체크아웃 예정 및 투숙 현황 리스트를 조회합니다.")
        public ApiResponse<PageResponse<OperationBoardResponse>> getTodayOperations(
                        @Parameter(hidden = true) @AuthenticationPrincipal CustomUser customUser,
                        PageRequest page,
                        SortRequest sort,
                        @Parameter(description = "요약 타입 (ARR, DEP, STAY)") @RequestParam(required = false) String summaryType,
                        @Parameter(description = "지점 코드") @RequestParam(required = false) Long propertyCode,
                        @Parameter(description = "고객명") @RequestParam(required = false) String customerName,
                        @Parameter(description = "예약 코드") @RequestParam(required = false) String reservationCode) {

                if (sort == null || sort.getSortBy() == null) {
                        sort = new SortRequest();
                        sort.setSortBy("r.checkout_date");
                        sort.setDirection("DESC");
                }

                return ApiResponse.success(
                                todayOperationQueryService.findTodayOperations(
                                                page,
                                                customUser.getHotelGroupCode(),
                                                propertyCode,
                                                summaryType,
                                                customerName,
                                                reservationCode,
                                                sort));
        }

        // 오늘의 예약정보 카운트(체크인예정 ,체크아웃예정, 투숙중 상단 숫자카드)
        @GetMapping("/today/operations/summary")
        @PreAuthorize("hasAuthority('TODAY_RESERVATION_LIST')")
        @Operation(summary = "오늘의 예약 운영 현황 요약 리스트 조회", description = "오늘의 체크인/체크아웃/투숙 카운트 요약을 리스트 조회합니다.")
        public ApiResponse<Map<String, Long>> getTodayOperationSummary(
                        @Parameter(hidden = true) @AuthenticationPrincipal CustomUser customUser,
                        @Parameter(description = "지점 코드") @RequestParam(required = false) Long propertyCode) {
                return ApiResponse.success(
                                todayOperationQueryService.getTodayOperationSummary(
                                                customUser.getHotelGroupCode(),
                                                propertyCode));
        }

}
