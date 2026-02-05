-- Inquiry Index
-- (property_code, inquiry_status, inquiry_code)
CREATE INDEX idx_inquiry_property_status_created ON inquiry (property_code, inquiry_status, created_at, inquiry_code);
