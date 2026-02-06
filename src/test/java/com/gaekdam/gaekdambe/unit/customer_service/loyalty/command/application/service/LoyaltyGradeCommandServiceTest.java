package com.gaekdam.gaekdambe.unit.customer_service.loyalty.command.application.service;

import com.gaekdam.gaekdambe.customer_service.loyalty.command.appliaction.dto.request.LoyaltyGradeRequest;
import com.gaekdam.gaekdambe.customer_service.loyalty.command.appliaction.service.LoyaltyGradeCommandService;
import com.gaekdam.gaekdambe.customer_service.loyalty.command.domain.LoyaltyGradeStatus;
import com.gaekdam.gaekdambe.customer_service.loyalty.command.domain.entity.LoyaltyGrade;
import com.gaekdam.gaekdambe.customer_service.loyalty.command.infrastructure.repository.LoyaltyGradeRepository;
import com.gaekdam.gaekdambe.global.exception.CustomException;
import com.gaekdam.gaekdambe.global.exception.ErrorCode;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.domain.entity.HotelGroup;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.infrastructure.repository.HotelGroupRepository;
import com.gaekdam.gaekdambe.iam_service.employee.command.infrastructure.EmployeeRepository;
import com.gaekdam.gaekdambe.iam_service.log.command.application.service.AuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoyaltyGradeCommandServiceTest {

        private static final String REASON = "UNIT_TEST_REASON";

        private HotelGroupRepository hotelGroupRepository;
        private LoyaltyGradeRepository loyaltyGradeRepository;
        private EmployeeRepository employeeRepository;
        private AuditLogService auditLogService;

        private LoyaltyGradeCommandService service;

        @BeforeEach
        void setUp() {
                hotelGroupRepository = mock(HotelGroupRepository.class);
                loyaltyGradeRepository = mock(LoyaltyGradeRepository.class);
                employeeRepository = mock(EmployeeRepository.class);
                auditLogService = mock(AuditLogService.class);

                service = new LoyaltyGradeCommandService(
                                hotelGroupRepository,
                                loyaltyGradeRepository,
                                employeeRepository,
                                auditLogService);
        }

        @Test
        @DisplayName("create: 호텔그룹 없으면 HOTEL_GROUP_NOT_FOUND")
        void create_hotelGroupNotFound_thenThrow() {
                // given
                Long hotelGroupCode = 1L;
                LoyaltyGradeRequest req = mock(LoyaltyGradeRequest.class);
                when(hotelGroupRepository.findById(hotelGroupCode)).thenReturn(Optional.empty());

                // when
                CustomException ex = catchThrowableOfType(
                                () -> service.createLoyaltyGrade(req, hotelGroupCode),
                                CustomException.class);

                // then
                assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.HOTEL_GROUP_NOT_FOUND);
                verify(hotelGroupRepository).findById(hotelGroupCode);
                verifyNoInteractions(loyaltyGradeRepository);
                verifyNoMoreInteractions(hotelGroupRepository);
        }

        @Test
        @DisplayName("create: 정상 저장 + 메시지 반환")
        void create_success() {
                // given
                Long hotelGroupCode = 1L;
                HotelGroup hotelGroup = mock(HotelGroup.class);
                when(hotelGroupRepository.findById(hotelGroupCode)).thenReturn(Optional.of(hotelGroup));

                LoyaltyGradeRequest req = mock(LoyaltyGradeRequest.class);
                when(req.loyaltyGradeName()).thenReturn("EXCELLENT");
                when(req.loyaltyTierLevel()).thenReturn(3L);
                when(req.loyaltyTierComment()).thenReturn("comment");
                when(req.loyaltyCalculationAmount()).thenReturn(100000L);
                when(req.loyaltyCalculationCount()).thenReturn(3);

                // when
                String msg = service.createLoyaltyGrade(req, hotelGroupCode);

                // then
                assertThat(msg).isEqualTo("멤버십 등급 생성 완료");

                ArgumentCaptor<LoyaltyGrade> captor = ArgumentCaptor.forClass(LoyaltyGrade.class);
                verify(loyaltyGradeRepository).save(captor.capture());

                LoyaltyGrade saved = captor.getValue();
                assertThat(saved.getLoyaltyGradeName()).isEqualTo("EXCELLENT");
                assertThat(saved.getLoyaltyTierLevel()).isEqualTo(3L);
                assertThat(saved.getLoyaltyTierComment()).isEqualTo("comment");
                assertThat(saved.getLoyaltyCalculationAmount()).isEqualTo(100000L);
                assertThat(saved.getLoyaltyCalculationCount()).isEqualTo(3);
                assertThat(saved.getLoyaltyCalculationTermMonth()).isEqualTo(12);
                assertThat(saved.getLoyaltyCalculationRenewalDay()).isEqualTo(1);
                assertThat(saved.getLoyaltyGradeStatus()).isEqualTo(LoyaltyGradeStatus.ACTIVE);

                verify(hotelGroupRepository).findById(hotelGroupCode);
                verifyNoMoreInteractions(hotelGroupRepository, loyaltyGradeRepository);
        }

        @Test
        @DisplayName("delete: 등급 없으면 LOYALTY_GRADE_NOT_FOUND")
        void delete_gradeNotFound_thenThrow() {
                // given
                Long hotelGroupCode = 1L;
                Long loyaltyGradeCode = 10L;
                when(loyaltyGradeRepository.findById(loyaltyGradeCode)).thenReturn(Optional.empty());

                // when
                CustomException ex = catchThrowableOfType(
                                () -> service.deleteLoyaltyGrade(hotelGroupCode, loyaltyGradeCode),
                                CustomException.class);

                // then
                assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.LOYALTY_GRADE_NOT_FOUND);
                verify(loyaltyGradeRepository).findById(loyaltyGradeCode);
                verifyNoMoreInteractions(loyaltyGradeRepository);
                verifyNoInteractions(hotelGroupRepository);
        }

        @Test
        @DisplayName("delete: 호텔그룹코드 불일치면 HOTEL_GROUP_CODE_NOT_MATCH")
        void delete_hotelGroupCodeNotMatch_thenThrow() {
                // given
                Long hotelGroupCode = 1L;
                Long loyaltyGradeCode = 10L;

                HotelGroup otherGroup = mock(HotelGroup.class);
                when(otherGroup.getHotelGroupCode()).thenReturn(999L);

                LoyaltyGrade grade = LoyaltyGrade.registerLoyaltyGrade(
                                otherGroup, "EXCELLENT", 3L, "c", 1L, 1);

                when(loyaltyGradeRepository.findById(loyaltyGradeCode)).thenReturn(Optional.of(grade));

                // when
                CustomException ex = catchThrowableOfType(
                                () -> service.deleteLoyaltyGrade(hotelGroupCode, loyaltyGradeCode),
                                CustomException.class);

                // then
                assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.HOTEL_GROUP_CODE_NOT_MATCH);
                verify(loyaltyGradeRepository).findById(loyaltyGradeCode);
                verify(loyaltyGradeRepository, never()).save(any());
                verifyNoMoreInteractions(loyaltyGradeRepository);
                verifyNoInteractions(hotelGroupRepository);
        }

        @Test
        @DisplayName("delete: 이미 INACTIVE면 LOYALTY_GRADE_ALREADY_INACTIVE")
        void delete_alreadyInactive_thenThrow() {
                // given
                Long hotelGroupCode = 1L;
                Long loyaltyGradeCode = 10L;

                HotelGroup group = mock(HotelGroup.class);
                when(group.getHotelGroupCode()).thenReturn(hotelGroupCode);

                LoyaltyGrade grade = LoyaltyGrade.registerLoyaltyGrade(
                                group, "EXCELLENT", 3L, "c", 1L, 1);
                grade.deleteLoyaltyGradeStatus();

                when(loyaltyGradeRepository.findById(loyaltyGradeCode)).thenReturn(Optional.of(grade));

                // when
                CustomException ex = catchThrowableOfType(
                                () -> service.deleteLoyaltyGrade(hotelGroupCode, loyaltyGradeCode),
                                CustomException.class);

                // then
                assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.LOYALTY_GRADE_ALREADY_INACTIVE);
                verify(loyaltyGradeRepository).findById(loyaltyGradeCode);
                verify(loyaltyGradeRepository, never()).save(any());
                verifyNoMoreInteractions(loyaltyGradeRepository);
                verifyNoInteractions(hotelGroupRepository);
        }

        @Test
        @DisplayName("delete: 정상 INACTIVE 변경 + save + 메시지 반환")
        void delete_success() {
                // given
                Long hotelGroupCode = 1L;
                Long loyaltyGradeCode = 10L;

                HotelGroup group = mock(HotelGroup.class);
                when(group.getHotelGroupCode()).thenReturn(hotelGroupCode);

                LoyaltyGrade grade = LoyaltyGrade.registerLoyaltyGrade(
                                group, "EXCELLENT", 3L, "c", 1L, 1);
                when(loyaltyGradeRepository.findById(loyaltyGradeCode)).thenReturn(Optional.of(grade));

                // when
                String msg = service.deleteLoyaltyGrade(hotelGroupCode, loyaltyGradeCode);

                // then
                assertThat(msg).isEqualTo("멤버십이 등급이 삭제 되었습니다");
                assertThat(grade.getLoyaltyGradeStatus()).isEqualTo(LoyaltyGradeStatus.INACTIVE);

                verify(loyaltyGradeRepository).findById(loyaltyGradeCode);
                verify(loyaltyGradeRepository).save(grade);
                verifyNoMoreInteractions(loyaltyGradeRepository);
                verifyNoInteractions(hotelGroupRepository);
        }

        @Test
        @DisplayName("update: 등급 없으면 LOYALTY_GRADE_NOT_FOUND")
        void update_gradeNotFound_thenThrow() {
                // given
                Long hotelGroupCode = 1L;
                Long loyaltyGradeCode = 10L;
                LoyaltyGradeRequest req = mock(LoyaltyGradeRequest.class);
                when(loyaltyGradeRepository.findById(loyaltyGradeCode)).thenReturn(Optional.empty());

                // when
                CustomException ex = catchThrowableOfType(
                                () -> service.updateLoyaltyGrade(hotelGroupCode, loyaltyGradeCode, req, REASON),
                                CustomException.class);

                // then
                assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.LOYALTY_GRADE_NOT_FOUND);
                verify(loyaltyGradeRepository).findById(loyaltyGradeCode);
                verifyNoMoreInteractions(loyaltyGradeRepository);
                verifyNoInteractions(hotelGroupRepository);
        }

        @Test
        @DisplayName("update: 호텔그룹코드 불일치면 HOTEL_GROUP_CODE_NOT_MATCH")
        void update_hotelGroupCodeNotMatch_thenThrow() {
                // given
                Long hotelGroupCode = 1L;
                Long loyaltyGradeCode = 10L;

                HotelGroup otherGroup = mock(HotelGroup.class);
                when(otherGroup.getHotelGroupCode()).thenReturn(999L);

                LoyaltyGrade grade = LoyaltyGrade.registerLoyaltyGrade(
                                otherGroup, "OLD", 1L, "old", 1L, 1);
                when(loyaltyGradeRepository.findById(loyaltyGradeCode)).thenReturn(Optional.of(grade));

                LoyaltyGradeRequest req = mock(LoyaltyGradeRequest.class);

                // when
                CustomException ex = catchThrowableOfType(
                                () -> service.updateLoyaltyGrade(hotelGroupCode, loyaltyGradeCode, req, REASON),
                                CustomException.class);

                // then
                assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.HOTEL_GROUP_CODE_NOT_MATCH);
                verify(loyaltyGradeRepository).findById(loyaltyGradeCode);
                verifyNoMoreInteractions(loyaltyGradeRepository);
                verifyNoInteractions(hotelGroupRepository);
        }

        @Test
        @DisplayName("update: 정상 업데이트 + 메시지 반환 (save 호출 없음)")
        void update_success() {
                // given
                Long hotelGroupCode = 1L;
                Long loyaltyGradeCode = 10L;

                HotelGroup group = mock(HotelGroup.class);
                when(group.getHotelGroupCode()).thenReturn(hotelGroupCode);

                LoyaltyGrade grade = LoyaltyGrade.registerLoyaltyGrade(
                                group, "OLD", 1L, "old", 1L, 1);
                when(loyaltyGradeRepository.findById(loyaltyGradeCode)).thenReturn(Optional.of(grade));

                LoyaltyGradeRequest req = mock(LoyaltyGradeRequest.class);
                when(req.loyaltyGradeName()).thenReturn("NEW");
                when(req.loyaltyTierLevel()).thenReturn(2L);
                when(req.loyaltyTierComment()).thenReturn("new");
                when(req.loyaltyCalculationAmount()).thenReturn(200L);
                when(req.loyaltyCalculationCount()).thenReturn(2);

                // when
                String msg = service.updateLoyaltyGrade(hotelGroupCode, loyaltyGradeCode, req, REASON);

                // then
                assertThat(msg).isEqualTo("등급 정보가 수정 되었습니다");
                assertThat(grade.getLoyaltyGradeName()).isEqualTo("NEW");
                assertThat(grade.getLoyaltyTierLevel()).isEqualTo(2L);
                assertThat(grade.getLoyaltyTierComment()).isEqualTo("new");
                assertThat(grade.getLoyaltyCalculationAmount()).isEqualTo(200L);
                assertThat(grade.getLoyaltyCalculationCount()).isEqualTo(2);
                assertThat(grade.getLoyaltyCalculationTermMonth()).isEqualTo(12);
                assertThat(grade.getLoyaltyCalculationRenewalDay()).isEqualTo(1);
        }
}
