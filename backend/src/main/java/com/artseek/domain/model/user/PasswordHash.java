package com.artseek.domain.model.user;

import com.artseek.domain.exceptions.user.InvalidPasswordHashException;
import jakarta.persistence.Embeddable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An encoded Argon2id password hash in PHC string format:
 * {@code $argon2id$v=19$m=<memory KiB>,t=<iterations>,p=<parallelism>$<salt>$<hash>}.
 *
 * <p>The single encoded value carries the algorithm, version, cost parameters, salt and hash
 * needed for verification and future rehashing. Only encoded hashes are accepted, so a plaintext
 * password cannot be stored. {@link #toString()} never reveals the encoded value.
 */
@Embeddable
public record PasswordHash(String encoded) {

    public static final String ALGORITHM = "argon2id";
    public static final int MAX_LENGTH = 255;

    private static final Pattern FORMAT = Pattern.compile(
            "\\$argon2id\\$v=(\\d+)\\$m=(\\d+),t=(\\d+),p=(\\d+)\\$[A-Za-z0-9+/]+\\$[A-Za-z0-9+/]+");

    public PasswordHash {
        if (encoded == null || encoded.length() > MAX_LENGTH || !FORMAT.matcher(encoded).matches()) {
            throw new InvalidPasswordHashException();
        }
    }

    public String algorithm() {
        return ALGORITHM;
    }

    public int version() {
        return parameter(1);
    }

    public int memoryCostKib() {
        return parameter(2);
    }

    public int iterations() {
        return parameter(3);
    }

    public int parallelism() {
        return parameter(4);
    }

    @Override
    public String toString() {
        return "PasswordHash[PROTECTED]";
    }

    private int parameter(int group) {
        Matcher matcher = FORMAT.matcher(encoded);
        matcher.matches();
        return Integer.parseInt(matcher.group(group));
    }
}
