package com.site.elohim.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CreateMemberRequest {
    private String memberName;

    // (yyyy-MM-dd 형식으로 JSON 들어오면 자동 LocalDate 변환)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate memberBirth;
    private String memberPhoneNumber;
    private String memberBaptism;
    private String nurtureYear;
    private String nurtureSemester;
    private String growthYear;
    private String growthSemester;
    private Long cellKey;
    private String cellName;
}
