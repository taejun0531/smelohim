// com.site.elohim.dto.CheckMemberRequest

package com.site.elohim.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckMemberRequest {

    private String memberName;
    private String memberBirth;   // "yyyy-MM-dd" 또는 null/빈 문자열
}
