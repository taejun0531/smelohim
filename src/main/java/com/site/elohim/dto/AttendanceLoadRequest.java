package com.site.elohim.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class AttendanceLoadRequest {

    private LocalDate attendanceDate;
    private List<Long> attendingMemberIdList;
}
