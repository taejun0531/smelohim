package com.site.elohim.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class UpdateMemberRequest {

    private Long id;
    private String memberName;
    private LocalDate memberBirth;        // "yyyy-MM-dd" 문자열이면 자동으로 매핑됨
    private String memberPhoneNumber;
    private String memberAddress;
    private String baptismStatus;

    private String nurtureYear;
    private String nurtureSemester;
    private String growthYear;
    private String growthSemester;

    private String memberMemo;

    private Boolean cellLeaderStatus;     // Boolean (wrapper 타입)
    private Long cellKey;              // null 가능
    private String cellName;

    // 기본 생성자 + getter/setter 전부
    // (IDE로 자동 생성)
}
