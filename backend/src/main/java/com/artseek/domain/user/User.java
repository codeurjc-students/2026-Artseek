package com.artseek.domain.user;

import com.artseek.domain.user.exceptions.InvalidRoleException;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * A registered account. Ordinary registration always grants {@link Role#USER}; administrators
 * can only be created through the explicit {@link #createAdministrator} factory.
 */
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = User.USERNAME_CONSTRAINT, columnNames = "username"),
        @UniqueConstraint(name = User.EMAIL_CONSTRAINT, columnNames = "email")
})
public class User {

    public static final String USERNAME_CONSTRAINT = "uq_users_username";
    public static final String EMAIL_CONSTRAINT = "uq_users_email";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @AttributeOverride(name = "value",
            column = @Column(name = "username", nullable = false, length = Username.MAX_LENGTH))
    private Username username;

    @Embedded
    @AttributeOverride(name = "value",
            column = @Column(name = "email", nullable = false, length = Email.MAX_LENGTH))
    private Email email;

    @Embedded
    @AttributeOverride(name = "encoded",
            column = @Column(name = "password_hash", nullable = false, length = PasswordHash.MAX_LENGTH))
    private PasswordHash passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    /** Required by JPA. Domain code should use the factories. */
    protected User() {
    }

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
