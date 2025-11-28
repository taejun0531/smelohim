package com.site.elohim.model;

import lombok.Getter;

@Getter
public enum Role {
    ADMIN("ROLE_ADMIN"),
    USER("ROLE_USER"),
    AWAIT("ROLE_AWAIT");

    private final String value;

    Role(String value) {
        this.value = value;
    }
}
