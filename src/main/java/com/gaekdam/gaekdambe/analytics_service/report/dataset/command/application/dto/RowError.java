package com.gaekdam.gaekdambe.analytics_service.report.dataset.command.application.dto;



public class RowError {

    private int row;


    private String message;

    public RowError() {
    }

    public RowError(int row, String message) {
        this.row = row;
        this.message = message;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}