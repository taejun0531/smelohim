// com.site.elohim.dto.PersonalDataSearchRequest

package com.site.elohim.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PersonalDataSearchRequest {

    private String findCategory;

    private String findName;

    private String findBirth_year;
    private String findBirth_month;

    private String findCellLeader;

    private String findBaptism;

    private String findNurture_year;
    private String findNurture_semester;

    private String findGrowth_year;
    private String findGrowth_semester;

    private String sortId; // "0" or "1"
}