-- property
CREATE INDEX idx_property_hotel_group
    ON property (hotel_group_code, property_code);

-- reservation
CREATE INDEX idx_reservation_operation
    ON reservation (
                    property_code,
                    reservation_status,
                    checkin_date,
                    checkout_date,
                    reservation_code
        );

-- stay
CREATE INDEX idx_stay_reservation_status
    ON stay (
             reservation_code,
             actual_checkin_at,
             actual_checkout_at,
             stay_code
        );

-- facility_usage
CREATE INDEX idx_facility_usage_stay_time
    ON facility_usage (
                       stay_code,
                       usage_at
        );