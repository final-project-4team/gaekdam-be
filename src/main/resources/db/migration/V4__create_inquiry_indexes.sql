CREATE INDEX idx_inq_employee_created
    ON inquiry (employee_code, created_at, inquiry_code);

CREATE INDEX idx_inq_property_created
    ON inquiry (property_code, created_at, inquiry_code);

CREATE INDEX idx_inq_property_updated
    ON inquiry (property_code, updated_at, inquiry_code);