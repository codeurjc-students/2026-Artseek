package com.artseek.domain.model.user;

import com.artseek.domain.exceptions.user.InvalidEmailException;
import jakarta.persistence.Embeddable;
import java.util.Locale;
import java.util.regex.Pattern;

/** A unique account email address, normalized to trimmed lowercase. */
@Embeddable
public record Email(String value) {

    public static final int MAX_LENGTH = 254;

    private static final Pattern FORMAT = Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");

    public Email {
        value = normalize(value);
    }

    private static String normalize(String value) {
        if (value == null) {
            throw new InvalidEmailException(null);
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (normalized.length() > MAX_LENGTH || !FORMAT.matcher(normalized).matches()) {
            throw new InvalidEmailException(value);
        }
        return normalized;
    }
}
