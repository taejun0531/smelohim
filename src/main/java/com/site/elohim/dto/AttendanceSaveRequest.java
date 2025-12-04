package com.site.elohim.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AttendanceSaveRequest {

    private List<AttendanceItemDto> items;
}
