-- 공통 필터: hotel_group_code + 정렬(created_at / customer_code / updated_at)
CREATE INDEX idx_customer_hotel_created_code
    ON customer (hotel_group_code, created_at, customer_code);

CREATE INDEX idx_customer_hotel_contract_code
    ON customer (hotel_group_code, contract_type, customer_code);

CREATE INDEX idx_customer_hotel_nation_code
    ON customer (hotel_group_code, nationality_type, customer_code);

-- 해시 검색
CREATE INDEX idx_customer_hotel_namehash
    ON customer (hotel_group_code, customer_name_hash);

-- 대표 연락처 뽑기(정렬용) + customerCode로 묶음
CREATE INDEX idx_cc_customer_primary_type_updated
    ON customer_contact (customer_code, is_primary, contact_type, updated_at, contact_code);

-- phoneHash/emailHash EXISTS 최적화
CREATE INDEX idx_cc_customer_type_hash
    ON customer_contact (customer_code, contact_type, contact_value_hash);


-- membership: ACTIVE 최신 1건 + (hotel_group_code 조인)
CREATE INDEX idx_m_hotel_customer_status_updated
    ON membership (hotel_group_code, customer_code, membership_status, updated_at, membership_code);

-- loyalty: ACTIVE 최신 1건 + (hotel_group_code 조인)
CREATE INDEX idx_l_hotel_customer_status_updated
    ON loyalty (hotel_group_code, customer_code, loyalty_status, updated_at, loyalty_code);

CREATE INDEX idx_mg_hotel_grade
    ON membership_grade (hotel_group_code, membership_grade_code);

CREATE INDEX idx_lg_hotel_grade
    ON loyalty_grade (hotel_group_code, loyalty_grade_code);