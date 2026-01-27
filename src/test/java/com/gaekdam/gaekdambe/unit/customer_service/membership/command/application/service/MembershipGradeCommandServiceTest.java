package com.gaekdam.gaekdambe.unit.customer_service.membership.command.application.service;


import com.gaekdam.gaekdambe.customer_service.membership.command.application.dto.request.MembershipGradeRequest;
import com.gaekdam.gaekdambe.customer_service.membership.command.application.service.MembershipGradeCommandService;
import com.gaekdam.gaekdambe.customer_service.membership.command.domain.MembershipGradeStatus;
import com.gaekdam.gaekdambe.customer_service.membership.command.domain.entity.MembershipGrade;
import com.gaekdam.gaekdambe.customer_service.membership.command.infrastructure.repository.MembershipGradeRepository;
import com.gaekdam.gaekdambe.global.exception.CustomException;
import com.gaekdam.gaekdambe.global.exception.ErrorCode;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.domain.entity.HotelGroup;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.infrastructure.repository.HotelGroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MembershipGradeCommandServiceTest {

    private HotelGroupRepository hotelGroupRepository;
    private MembershipGradeRepository membershipGradeRepository;

    private MembershipGradeCommandService service;

    @BeforeEach
    void setUp() {
        hotelGroupRepository = mock(HotelGroupRepository.class);
        membershipGradeRepository = mock(MembershipGradeRepository.class);
        service = new MembershipGradeCommandService(hotelGroupRepository, membershipGradeRepository);
    }

    @Test
    @DisplayName("create: gradeName null이면 INVALID_INCORRECT_FORMAT")
    void create_invalid_gradeName() {
        MembershipGradeRequest req = new MembershipGradeRequest(
                null, 1L, "comment", 1000L, 1, 12, 1
        );

        CustomException ex = catchThrowableOfType(
                () -> service.createMembershipGrade(req, 1L),
                CustomException.class
        );

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_INCORRECT_FORMAT);
        verifyNoInteractions(hotelGroupRepository, membershipGradeRepository);
    }

    @Test
    @DisplayName("create: 필수값 통과 + 호텔그룹 존재하면 save 호출하고 성공 메시지 반환")
    void create_success() {
        Long hotelGroupCode = 1L;
        MembershipGradeRequest req = new MembershipGradeRequest(
                "GOLD", 2L, "tier", 1000L, 1, 12, 1
        );

        HotelGroup hotelGroup = mock(HotelGroup.class);
        when(hotelGroupRepository.findById(hotelGroupCode)).thenReturn(Optional.of(hotelGroup));

        String result = service.createMembershipGrade(req, hotelGroupCode);

        assertThat(result).isEqualTo("멤버십 등급 생성 완료");
        verify(membershipGradeRepository).save(any(MembershipGrade.class));
    }

    @Test
    @DisplayName("create: 호텔그룹 없으면 IllegalArgumentException")
    void create_hotelGroup_notFound() {
        Long hotelGroupCode = 1L;
        MembershipGradeRequest req = new MembershipGradeRequest(
                "GOLD", 2L, "tier", 1000L, 1, 12, 1
        );

        when(hotelGroupRepository.findById(hotelGroupCode)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createMembershipGrade(req, hotelGroupCode))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Hotel code not found");

        verifyNoInteractions(membershipGradeRepository);
    }

    @Test
    @DisplayName("delete: 호텔그룹 코드 불일치면 HOTEL_GROUP_CODE_NOT_MATCH")
    void delete_hotelGroup_mismatch() {
        Long hotelGroupCode = 1L;
        Long gradeCode = 10L;

        HotelGroup hg = mock(HotelGroup.class);
        when(hg.getHotelGroupCode()).thenReturn(999L);

        MembershipGrade grade = MembershipGrade.registerMembershipGrade(
                hg, "GOLD", 1L, "tier", 1000L, 1, 12, 1
        );

        when(membershipGradeRepository.findById(gradeCode)).thenReturn(Optional.of(grade));

        CustomException ex = catchThrowableOfType(
                () -> service.deleteMembershipGrade(hotelGroupCode, gradeCode),
                CustomException.class
        );

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.HOTEL_GROUP_CODE_NOT_MATCH);
        verify(membershipGradeRepository, never()).save(any());
    }

    @Test
    @DisplayName("delete: 이미 INACTIVE면 INVALID_REQUEST")
    void delete_already_inactive() {
        Long hotelGroupCode = 1L;
        Long gradeCode = 10L;

        HotelGroup hg = mock(HotelGroup.class);
        when(hg.getHotelGroupCode()).thenReturn(hotelGroupCode);

        MembershipGrade grade = MembershipGrade.registerMembershipGrade(
                hg, "GOLD", 1L, "tier", 1000L, 1, 12, 1
        );
        grade.deleteMemberShipGradeStatus(); // INACTIVE로 만듦

        when(membershipGradeRepository.findById(gradeCode)).thenReturn(Optional.of(grade));

        CustomException ex = catchThrowableOfType(
                () -> service.deleteMembershipGrade(hotelGroupCode, gradeCode),
                CustomException.class
        );

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
        verify(membershipGradeRepository, never()).save(any());
    }

    @Test
    @DisplayName("delete: ACTIVE면 INACTIVE로 변경 후 save 호출, 성공 메시지 반환")
    void delete_success() {
        Long hotelGroupCode = 1L;
        Long gradeCode = 10L;

        HotelGroup hg = mock(HotelGroup.class);
        when(hg.getHotelGroupCode()).thenReturn(hotelGroupCode);

        MembershipGrade grade = MembershipGrade.registerMembershipGrade(
                hg, "GOLD", 1L, "tier", 1000L, 1, 12, 1
        );

        when(membershipGradeRepository.findById(gradeCode)).thenReturn(Optional.of(grade));

        String result = service.deleteMembershipGrade(hotelGroupCode, gradeCode);

        assertThat(result).isEqualTo("멤버십이 등급이 삭제 되었습니다");
        assertThat(grade.getMembershipGradeStatus()).isEqualTo(MembershipGradeStatus.INACTIVE);
        verify(membershipGradeRepository).save(grade);
    }

    @Test
    @DisplayName("update: gradeName 빈값이면 INVALID_INCORRECT_FORMAT")
    void update_invalid_gradeName_blank() {
        MembershipGradeRequest req = new MembershipGradeRequest(
                " ", 1L, "tier", 1000L, 1, 12, 1
        );

        CustomException ex = catchThrowableOfType(
                () -> service.updateMembershipGrade(1L, 10L, req),
                CustomException.class
        );

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_INCORRECT_FORMAT);
        verifyNoInteractions(hotelGroupRepository, membershipGradeRepository);
    }

    @Test
    @DisplayName("update: 호텔그룹 코드 불일치면 HOTEL_GROUP_CODE_NOT_MATCH")
    void update_hotelGroup_mismatch() {
        Long hotelGroupCode = 1L;
        Long gradeCode = 10L;

        MembershipGradeRequest req = new MembershipGradeRequest(
                "GOLD", 2L, "tier", 1000L, 1, 12, 1
        );

        HotelGroup hg = mock(HotelGroup.class);
        when(hg.getHotelGroupCode()).thenReturn(999L);

        MembershipGrade grade = MembershipGrade.registerMembershipGrade(
                hg, "SILVER", 1L, "tier", 1000L, 1, 12, 1
        );

        when(membershipGradeRepository.findById(gradeCode)).thenReturn(Optional.of(grade));

        CustomException ex = catchThrowableOfType(
                () -> service.updateMembershipGrade(hotelGroupCode, gradeCode, req),
                CustomException.class
        );

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.HOTEL_GROUP_CODE_NOT_MATCH);
        verify(membershipGradeRepository, never()).save(any());
    }

    @Test
    @DisplayName("update: 정상 수정되면 grade.update 반영되고 성공 메시지 반환")
    void update_success() {
        Long hotelGroupCode = 1L;
        Long gradeCode = 10L;

        MembershipGradeRequest req = new MembershipGradeRequest(
                "GOLD", 2L, "new tier", 2000L, 2, 6, 15
        );

        HotelGroup hg = mock(HotelGroup.class);
        when(hg.getHotelGroupCode()).thenReturn(hotelGroupCode);

        MembershipGrade grade = MembershipGrade.registerMembershipGrade(
                hg, "SILVER", 1L, "tier", 1000L, 1, 12, 1
        );

        when(membershipGradeRepository.findById(gradeCode)).thenReturn(Optional.of(grade));

        String result = service.updateMembershipGrade(hotelGroupCode, gradeCode, req);

        assertThat(result).isEqualTo("등급 정보가 수정 되었습니다");
        assertThat(grade.getGradeName()).isEqualTo("GOLD");
        assertThat(grade.getTierLevel()).isEqualTo(2L);

        // update는 save를 호출하지 않고 트랜잭션 더티체킹을 기대하는 구조
        verify(membershipGradeRepository, never()).save(any());
    }
}

