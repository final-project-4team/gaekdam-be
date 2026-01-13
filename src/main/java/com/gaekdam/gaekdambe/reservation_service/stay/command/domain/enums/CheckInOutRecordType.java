package com.gaekdam.gaekdambe.reservation_service.stay.command.domain.enums;

import lombok.Getter;

@Getter
public enum CheckInOutRecordType {

    CHECK_IN("체크인"),
    CHECK_OUT("체크아웃");

    private final String title;

    CheckInOutRecordType(String title) {
        this.title = title;
    }
}
