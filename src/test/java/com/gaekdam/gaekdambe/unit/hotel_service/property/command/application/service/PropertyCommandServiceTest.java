package com.gaekdam.gaekdambe.unit.hotel_service.property.command.application.service;

import com.gaekdam.gaekdambe.global.exception.CustomException;
import com.gaekdam.gaekdambe.global.exception.ErrorCode;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.domain.entity.HotelGroup;
import com.gaekdam.gaekdambe.hotel_service.hotel.command.infrastructure.repository.HotelGroupRepository;
import com.gaekdam.gaekdambe.hotel_service.property.command.application.dto.request.PropertyRequest;
import com.gaekdam.gaekdambe.hotel_service.property.command.application.service.PropertyCommandService;
import com.gaekdam.gaekdambe.hotel_service.property.command.domain.PropertyStatus;
import com.gaekdam.gaekdambe.hotel_service.property.command.domain.entity.Property;
import com.gaekdam.gaekdambe.hotel_service.property.command.infrastructure.PropertyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class PropertyCommandServiceTest {

    @InjectMocks
    private PropertyCommandService service;

    @Mock
    private PropertyRepository propertyRepository;
    @Mock
    private HotelGroupRepository hotelGroupRepository;

    @Test
    @DisplayName("createProperty: 지점 생성 성공")
    void createProperty_success() {
        // given
        PropertyRequest request = new PropertyRequest("NewProp", "Seoul");
        Long hgCode = 1L;
        HotelGroup hg = HotelGroup.builder().hotelGroupCode(hgCode).build();
        given(hotelGroupRepository.findById(hgCode)).willReturn(Optional.of(hg));

        // when
        String result = service.createProperty(request, hgCode);

        // then
        assertThat(result).isEqualTo("지점 추가 성공");
        verify(propertyRepository).save(any(Property.class));
    }

    @Test
    @DisplayName("updateProperty: 지점 수정 성공")
    void updateProperty_success() {
        // given
        Long pCode = 10L;
        PropertyRequest request = new PropertyRequest("UpdatedProp", "Busan");
        Property property = Property.builder().propertyCode(pCode).build(); // Mock object behavior using spy/mock if
                                                                            // logic complex, but here simplistic
        // Since Property.updateProperty modifies state, we can use a real object for
        // entity or verify fields if possible?
        // Service just retrieves and calls update.
        // But Property entity methods are void.
        // We can just verify repo findById and no exception.

        // Re-mocking property with simple object to avoid complexity
        given(propertyRepository.findById(pCode)).willReturn(Optional.of(property));

        // when
        String result = service.updateProperty(pCode, request);

        // then
        assertThat(result).isEqualTo("지점 정보가 수정 되었습니다");
        assertThat(property.getPropertyName()).isEqualTo("UpdatedProp");
        assertThat(property.getPropertyCity()).isEqualTo("Busan");
    }

    @Test
    @DisplayName("deleteProperty: 지점 삭제 성공")
    void deleteProperty_success() {
        // given
        Long pCode = 10L;
        Property property = Property.builder().propertyCode(pCode).build();
        given(propertyRepository.findById(pCode)).willReturn(Optional.of(property));

        // when
        String result = service.deleteProperty(pCode);

        // then
        assertThat(result).isEqualTo("삭제 되었습니다");
        // verify delete logic if it calls repo or just status change
        // property.deleteProperty() likely changes status.
    }

    // ========== Exception Tests ==========

    @Test
    @DisplayName("createProperty: propertyName이 null이면 예외 발생")
    void createProperty_fail_nullName() {
        // given
        PropertyRequest request = new PropertyRequest(null, "Seoul");
        Long hgCode = 1L;

        // when & then
        assertThatThrownBy(() -> service.createProperty(request, hgCode))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INCORRECT_FORMAT);
    }

    @Test
    @DisplayName("createProperty: propertyName이 빈 문자열이면 예외 발생")
    void createProperty_fail_emptyName() {
        // given
        PropertyRequest request = new PropertyRequest("", "Seoul");
        Long hgCode = 1L;

        // when & then
        assertThatThrownBy(() -> service.createProperty(request, hgCode))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INCORRECT_FORMAT);
    }

    @Test
    @DisplayName("createProperty: propertyCity가 null이면 예외 발생")
    void createProperty_fail_nullCity() {
        // given
        PropertyRequest request = new PropertyRequest("NewProp", null);
        Long hgCode = 1L;

        // when & then
        assertThatThrownBy(() -> service.createProperty(request, hgCode))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INCORRECT_FORMAT);
    }

    @Test
    @DisplayName("createProperty: propertyCity가 빈 문자열이면 예외 발생")
    void createProperty_fail_emptyCity() {
        // given
        PropertyRequest request = new PropertyRequest("NewProp", "");
        Long hgCode = 1L;

        // when & then
        assertThatThrownBy(() -> service.createProperty(request, hgCode))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INCORRECT_FORMAT);
    }

    @Test
    @DisplayName("updateProperty: name과 city 둘 다 null이면 예외 발생")
    void updateProperty_fail_bothNull() {
        // given
        Long pCode = 10L;
        PropertyRequest request = new PropertyRequest(null, null);

        // when & then
        assertThatThrownBy(() -> service.updateProperty(pCode, request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INCORRECT_FORMAT);
    }

    @Test
    @DisplayName("updateProperty: name과 city 둘 다 빈 문자열이면 예외 발생")
    void updateProperty_fail_bothEmpty() {
        // given
        Long pCode = 10L;
        PropertyRequest request = new PropertyRequest("", "");

        // when & then
        assertThatThrownBy(() -> service.updateProperty(pCode, request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INCORRECT_FORMAT);
    }

    @Test
    @DisplayName("deleteProperty: Property를 찾지 못하면 예외 발생")
    void deleteProperty_fail_notFound() {
        // given
        Long pCode = 999L;
        given(propertyRepository.findById(pCode)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> service.deleteProperty(pCode))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Property code not found");
    }

    @Test
    @DisplayName("deleteProperty: Property 상태가 CLOSED면 예외 발생")
    void deleteProperty_fail_alreadyClosed() {
        // given
        Long pCode = 10L;
        Property property = mock(Property.class);
        given(property.getPropertyStatus()).willReturn(PropertyStatus.CLOSED);
        given(propertyRepository.findById(pCode)).willReturn(Optional.of(property));

        // when & then
        assertThatThrownBy(() -> service.deleteProperty(pCode))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REQUEST);
    }
}
