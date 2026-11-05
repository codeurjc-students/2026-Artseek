package com.artseek.domain.user;

import com.artseek.domain.user.exceptions.InvalidRoleException;

/**
 * A registered account. Ordinary registration always grants {@link Role#USER}; administrators
 * can only be created through the explicit {@link #createAdministrator} factory.
 */
public class User {

    private final Long id;

    private final Username username;

    private final Email email;

    private PasswordHash passwordHash;

    private final Role role;

    private User(Long id, Username username, Email email, PasswordHash passwordHash, Role role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = validateRole(role);
    }

    /** Creates an ordinary account. Registration can never grant elevated permissions. */
    public static User register(Username username, Email email, PasswordHash passwordHash) {
        return new User(null, username, email, passwordHash, Role.USER);
    }

    /** Creates an administrator. Only trusted, non-public paths may call this factory. */
    public static User createAdministrator(Username username, Email email, PasswordHash passwordHash) {
        return new User(null, username, email, passwordHash, Role.ADMIN);
    }

    /**
     * Rebuilds an existing account from persisted values. The value objects validate their
     * input again, so stored data that breaks a current rule fails before reaching this factory.
     */
    public static User restore(Long id, Username username, Email email, PasswordHash passwordHash, Role role) {
        return new User(id, username, email, passwordHash, role);
    }

    public Long getId() {
        return id;
    }

    public Username getUsername() {
        return username;
    }

    public Email getEmail() {
        return email;
    }

    public PasswordHash getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public boolean isAdministrator() {
        return role == Role.ADMIN;
    }

    /** Replaces the stored hash, for example after a password change or a cost-parameter rehash. */
    public void changePasswordHash(PasswordHash passwordHash) {
        this.passwordHash = passwordHash;
    }

    @Override
    public String toString() {
        return "User[id=" + id + ", username=" + (username == null ? null : username.value())
                + ", role=" + role + "]";
    }

    private static Role validateRole(Role role) {
        if (role == null) {
            throw new InvalidRoleException(null);
        }
        return role;
    }
}
