package com.artseek.domain.user;

import com.artseek.domain.user.exceptions.InvalidRoleException;
import java.util.Arrays;

/** The closed set of account permission levels. Anonymous access is not a role. */
public enum Role {
    USER("user"),
    ADMIN("admin");

    private final String value;

    Role(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static Role fromValue(String value) {
        if (value == null) {
            throw new InvalidRoleException(null);
        }

        return Arrays.stream(values())
                .filter(role -> role.value.equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElseThrow(() -> new InvalidRoleException(value));
    }
}
