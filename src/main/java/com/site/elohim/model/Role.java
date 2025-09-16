package com.site.elohim.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public enum Role {
    ADMIN("ROLE_ADMIN"),
    USER("ROLE_USER"),
    AWAIT("ROLE_AWAIT");

    Role(String value) {
        this.value = value;
    }

    private String value;
}
