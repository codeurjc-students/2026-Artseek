package com.artseek.domain.user;

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
import java.time.Instant;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;

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

    public static final String USERNAME_CONSTRAINT = "uk_users_username";
    public static final String EMAIL_CONSTRAINT = "uk_users_email";

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

    @CreationTimestamp(source = SourceType.DB)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp(source = SourceType.DB)
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** Required by JPA. Domain code should use the factories. */
    protected User() {
    }

    private User(String username, String email, String passwordHash, Role role) {
        this.username = new Username(username);
        this.email = new Email(email);
        this.passwordHash = new PasswordHash(passwordHash);
        this.role = role;
    }

    /** Creates an ordinary account. Registration can never grant elevated permissions. */
    public static User register(String username, String email, String passwordHash) {
        return new User(username, email, passwordHash, Role.USER);
    }

    /** Creates an administrator. Only trusted, non-public paths may call this factory. */
    public static User createAdministrator(String username, String email, String passwordHash) {
        return new User(username, email, passwordHash, Role.ADMIN);
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /** Replaces the stored hash, for example after a password change or a cost-parameter rehash. */
    public void changePasswordHash(String passwordHash) {
        this.passwordHash = new PasswordHash(passwordHash);
    }

    @Override
    public String toString() {
        return "User[id=" + id + ", username=" + (username == null ? null : username.value())
                + ", role=" + role + "]";
    }
}
