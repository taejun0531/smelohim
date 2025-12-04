package com.site.elohim.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class AttendanceItemDto {

    private Long memberId;
    private LocalDate attendanceDate;
    private Boolean worshipStatus;
    private Boolean cellStatus;
    private String attendanceMemo;
}
