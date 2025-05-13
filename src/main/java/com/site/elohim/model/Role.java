package com.site.elohim.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Role {
    ADMIN("ROLE_ADMIN"),
    USER("ROLE_USER"),
    AWAIT("ROLE_AWAIT");

    private String value;
}
