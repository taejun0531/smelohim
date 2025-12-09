package com.site.elohim.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttendanceStatsItemDto {

    private Long memberId;
    private String memberName;

    private long absentCount;   // 결석 수
    private long worshipCount;  // 예배 출석 수
    private long cellCount;     // 셀모임 출석 수
}
