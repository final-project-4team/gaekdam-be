package com.gaekdam.gaekdambe.global.paging;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SortRequest {

    private String sortBy = "created_at"; // DB 컬럼명
    private String direction = "DESC";    // ASC / DESC
}