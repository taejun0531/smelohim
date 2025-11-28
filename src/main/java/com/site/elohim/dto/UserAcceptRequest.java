package com.site.elohim.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserAcceptRequest {

    private Long id;
    private String userRole;
    private Long leaderId;    // null 허용
}
