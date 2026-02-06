-- Incident Index
-- (property_code, incident_status, incident_code)
CREATE INDEX idx_incident_property_status_created ON incident (property_code, incident_status, created_at, incident_code);
