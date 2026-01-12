package com.gaekdam.gaekdambe.global.paging;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SortRequest {

    private String sortBy;
    private String direction = "DESC";    // ASC / DESC


}