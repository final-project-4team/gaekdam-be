package com.gaekdam.gaekdambe.communication_service.messaging.query.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessagingConditionContext {

    private Long reservationCode;
    private Long stayCode;

    private Long customerCode;
    private Integer guestCount;

    private Long membershipGradeCode;
    private Long propertyCode;

    private String reservationStatus;
    private String checkinDate;
    private String checkoutDate;

    private String actualCheckinAt;
    private String actualCheckoutAt;
}
