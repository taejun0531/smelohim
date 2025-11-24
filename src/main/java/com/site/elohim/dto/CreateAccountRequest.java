package com.site.elohim.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CreateAccountRequest {

    private String userId;
    private String userPassword;
    private String userCheckPassword;
    private String userName;

}
