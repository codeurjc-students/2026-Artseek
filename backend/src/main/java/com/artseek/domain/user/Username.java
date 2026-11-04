package com.artseek.domain.user;

import com.artseek.domain.user.exceptions.InvalidUsernameException;
import jakarta.persistence.Embeddable;
import java.util.Locale;
import java.util.regex.Pattern;

/** A unique public account identifier, normalized to trimmed lowercase. */
@Embeddable
public record Username(String value) {

    public static final int MIN_LENGTH = 3;
    public static final int MAX_LENGTH = 30;

    private static final Pattern ALLOWED_CHARACTERS = Pattern.compile("[a-z0-9._-]+");

    public Username {
        value = normalize(value);
    }

    private static String normalize(String value) {
        if (value == null) {
            throw new InvalidUsernameException(null);
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (normalized.length() < MIN_LENGTH
                || normalized.length() > MAX_LENGTH
                || !ALLOWED_CHARACTERS.matcher(normalized).matches()) {
            throw new InvalidUsernameException(value);
        }
        return normalized;
    }
}
