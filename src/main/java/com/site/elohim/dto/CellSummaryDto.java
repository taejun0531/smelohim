package com.site.elohim.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CellSummaryDto {

    private Long cellKey;   // 셀리더 member.id
    private String cellName; // 셀 이름 ("태준셀")
}
