package com.gaekdam.gaekdambe.analytics_service.report.dataset.command.application.dto;

import java.util.List;

public class ImportResultDto {
    private int created;
    private int updated;
    private int skipped;
    private List<RowError> errors;

    public ImportResultDto() {}

    public ImportResultDto(int created, int updated, int skipped, List<RowError> errors) {
        this.created = created;
        this.updated = updated;
        this.skipped = skipped;
        this.errors = errors;
    }

    public int getCreated() { return created; }
    public void setCreated(int created) { this.created = created; }

    public int getUpdated() { return updated; }
    public void setUpdated(int updated) { this.updated = updated; }

    public int getSkipped() { return skipped; }
    public void setSkipped(int skipped) { this.skipped = skipped; }

    public List<RowError> getErrors() { return errors; }
    public void setErrors(List<RowError> errors) { this.errors = errors; }
}