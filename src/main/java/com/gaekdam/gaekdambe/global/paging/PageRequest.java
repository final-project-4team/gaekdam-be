package com.gaekdam.gaekdambe.global.paging;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageRequest {

    private int page = 1;
    private int size = 20;

    public int getOffset() {
        return (page - 1) * size;
    }
}