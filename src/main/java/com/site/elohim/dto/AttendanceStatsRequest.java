package com.site.elohim.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class AttendanceStatsRequest {

    private Long cellKey;         // 조회할 셀
    private LocalDate startDate;  // 통계 시작
    private LocalDate endDate;    // 통계 종료
}
