package com.site.elohim.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class createAccountRequest {

    private String userId;
    private String userPassword;
    private String userCheckPassword;
    private String userName;

}
