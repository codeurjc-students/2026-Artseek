package com.artseek.application.user;

import com.artseek.domain.user.User;
import java.time.Instant;

/** Public account representation. Never carries the email address or credential data. */
public record UserDTO(Long id, String username, String role, Instant createdAt) {

    public static UserDTO from(User user) {
        return new UserDTO(
                user.getId(),
                user.getUsername().value(),
                user.getRole().value(),
                user.getCreatedAt());
    }
}
