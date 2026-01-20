package com.gaekdam.gaekdambe.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public enum ErrorCode {
  // 1000번대는 공통 오류 처리
  UNAUTHORIZED_ACCESS("1000", "관리자만 접근 가능합니다", HttpStatus.UNAUTHORIZED),
  INVALID_REQUEST("1001", "잘못된 요청입니다.", HttpStatus.BAD_REQUEST),
  INVALID_USER_ID("1002", "해당 사용자가 없습니다", HttpStatus.BAD_REQUEST),
  INVALID_ADMIN_ID("1003", "잘못된 관리자 ID입니다.", HttpStatus.BAD_REQUEST),
  INVALID_INCORRECT_FORMAT("1004", "잘못된 형식입니다.", HttpStatus.BAD_REQUEST),
  PASSWORD_NOT_MATCH("1005", "비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
  INVALID_INPUT_FORMAT("1006", "잘못된 입력 형식입니다.", HttpStatus.BAD_REQUEST),
  DUPLICATE_VALUE("1007", "중복된 값입니다.", HttpStatus.BAD_REQUEST),
  NOT_FOUND_VALUE("1008", "존재하지 않는 값입니다.", HttpStatus.NOT_FOUND),
  NULL_UNAUTHORIZED("1009", "로그인 정보가 없습니다", HttpStatus.UNAUTHORIZED),
  HOTEL_GROUP_CODE_NOT_MATCH("1010", "호텔 그룹 코드가 일치하지 않습니다", HttpStatus.BAD_REQUEST),
  HOTEL_GROUP_NOT_FOUND("1011", "호텔 그룹을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  PERMISSION_TYPE_NOT_FOUND("1012", "존재하지 않는 권한 타입이 포함되어 있습니다.", HttpStatus.BAD_REQUEST),
  PERMISSION_NOT_FOUND("1013", "권한을 찾을 수 없거나 접근 권한이 없습니다.", HttpStatus.NOT_FOUND),

  // 2000번대: Report / Dashboard 도메인 오류

  REPORT_LAYOUT_CREATE_ERROR("2001", "레이아웃 생성 에러가 발생하였습니다.", HttpStatus.BAD_REQUEST),
  REPORT_LAYOUT_NOT_FOUND("2002", "레이아웃을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  REPORT_LAYOUT_UPDATE_ERROR("2101", "레이아웃 수정 에러가 발생하였습니다.", HttpStatus.BAD_REQUEST),
  REPORT_LAYOUT_DELETE_ERROR("2201", "레이아웃 삭제 에러가 발생하였습니다.", HttpStatus.BAD_REQUEST),

  REPORT_KPI_CODE_NOT_FOUND("2301", "존재하지 않는 KPI 코드입니다.", HttpStatus.NOT_FOUND),
  REPORT_KPI_TARGET_CREATE_ERROR("2311", "KPI 목표 생성에 실패했습니다.", HttpStatus.BAD_REQUEST),
  REPORT_KPI_TARGET_ALREADY_EXISTS("2312", "이미 존재하는 KPI 목표입니다.", HttpStatus.CONFLICT),
  REPORT_KPI_TARGET_INVALID_PERIOD("2313", "기간 정보가 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
  REPORT_KPI_TARGET_NOT_FOUND("2321", "KPI 목표를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  REPORT_KPI_TARGET_UPDATE_ERROR("2331", "KPI 목표 수정에 실패했습니다.", HttpStatus.BAD_REQUEST),
  REPORT_KPI_TARGET_DELETE_ERROR("2341", "KPI 목표 삭제에 실패했습니다.", HttpStatus.BAD_REQUEST),

  // 3000번대 : Customer, MemberShip,Loyalty
  CUSTOMER_NOT_FOUND("3001", "고객을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  CUSTOMER_MEMO_NOT_FOUND("3002", "고객 메모를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  MEMBERSHIP_GRADE_NOT_FOUND( "3003","멤버십 등급을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  MEMBERSHIP_GRADE_INACTIVE("3004","비활성화된 멤버십 등급입니다.", HttpStatus.BAD_REQUEST),
  MEMBERSHIP_MANUAL_REASON_REQUIRED("3005","수동 변경 사유는 필수입니다.", HttpStatus.BAD_REQUEST),

  REPORT_TEMPLATE_NOT_FOUND("2401", "존재하지 않는 템플릿입니다.", HttpStatus.BAD_REQUEST),
  REPORT_LAYOUT_TEMPLATE_DUPLICATE("2402", "이미 해당 레이아웃에 등록된 템플릿입니다.", HttpStatus.CONFLICT),
  REPORT_LAYOUT_TEMPLATE_NOT_FOUND("2403", "레이아웃에 등록된 템플릿을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  UNAUTHORIZED("401", "만료된 토큰", HttpStatus.UNAUTHORIZED),

  RESERVATION_CONFLICT("4001", "선택하신 기간에 예약이 이미 존재합니다.", HttpStatus.BAD_REQUEST);



    private final String code;
  private final String message;
  private final HttpStatusCode httpStatusCode;

}
