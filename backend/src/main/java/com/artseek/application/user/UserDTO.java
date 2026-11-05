package com.artseek.application.user;

import com.artseek.domain.user.User;

/** Public account representation. Never carries the email address or credential data. */
public record UserDTO(Long id, String username, String role) {

    public static UserDTO from(User user) {
        return new UserDTO(
                user.getId(),
                user.getUsername().value(),
                user.getRole().value());
    }
}
