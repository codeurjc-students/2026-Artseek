package com.artseek.infrastructure.user.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.artseek.domain.user.Email;
import com.artseek.domain.user.PasswordHash;
import com.artseek.domain.user.Role;
import com.artseek.domain.user.User;
import com.artseek.domain.user.Username;
import com.artseek.domain.user.repository.UserRepository;
import com.artseek.shared.JpaIntegrationTest;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

@Import(UserRepositoryAdapter.class)
class UserRepositoryAdapterIntegrationTests extends JpaIntegrationTest {

    private static final String PASSWORD_HASH =
            "$argon2id$v=19$m=19456,t=2,p=1$c2FsdHNhbHRzYWx0c2FsdA$aGFzaGhhc2hoYXNoaGFzaGhhc2hoYXNoaGFzaGhhc2g";
    private static final String OTHER_PASSWORD_HASH =
            "$argon2id$v=19$m=47104,t=1,p=1$b3RoZXJzYWx0b3RoZXJzYWx0$b3RoZXJoYXNob3RoZXJoYXNob3RoZXJoYXNob3RoZXI";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void savesAUserAndAssignsItsIdentifier() {
        User savedUser = userRepository.save(newUser("ada", "ada@example.org"));

        assertThat(savedUser.getId()).isNotNull().isPositive();
    }

    @Test
    void retrievesAPersistedUserById() {
        User savedUser = userRepository.save(newUser("ada", "ada@example.org"));
        entityManager.flush();
        entityManager.clear();

        assertThat(userRepository.findById(savedUser.getId()))
                .isPresent()
                .get()
                .satisfies(user -> {
                    assertThat(user.getUsername()).isEqualTo(new Username("ada"));
                    assertThat(user.getEmail()).isEqualTo(new Email("ada@example.org"));
                    assertThat(user.getPasswordHash()).isEqualTo(new PasswordHash(PASSWORD_HASH));
                    assertThat(user.getRole()).isEqualTo(Role.USER);
                });
    }

    @Test
    void findsUsersByNormalizedUsernameAndEmail() {
        User savedUser = userRepository.save(newUser("Ada", "Ada@Example.org"));
        entityManager.flush();
        entityManager.clear();

        assertThat(userRepository.findByUsername(new Username("  ADA ")))
                .get()
                .extracting(User::getId)
                .isEqualTo(savedUser.getId());
        assertThat(userRepository.findByEmail(new Email(" ADA@EXAMPLE.ORG")))
                .get()
                .extracting(User::getId)
                .isEqualTo(savedUser.getId());
        assertThat(userRepository.findByUsername(new Username("grace"))).isEmpty();
        assertThat(userRepository.findByEmail(new Email("grace@example.org"))).isEmpty();
    }

    @Test
    void reportsWhetherUsernamesAndEmailsAreTaken() {
        userRepository.save(newUser("ada", "ada@example.org"));

        assertThat(userRepository.existsByUsername(new Username("ADA"))).isTrue();
        assertThat(userRepository.existsByEmail(new Email("ADA@example.org"))).isTrue();
        assertThat(userRepository.existsByUsername(new Username("grace"))).isFalse();
        assertThat(userRepository.existsByEmail(new Email("grace@example.org"))).isFalse();
    }

    @Test
    void storesNormalizedIdentifiersTheEncodedHashAndTheRoleName() {
        User savedUser = userRepository.save(newUser("  Ada ", " ADA@Example.org "));
        entityManager.flush();

        Map<String, Object> row = jdbcTemplate.queryForMap(
                "select username, email, password_hash, role from users where id = ?",
                savedUser.getId());

        assertThat(row)
                .containsEntry("username", "ada")
                .containsEntry("email", "ada@example.org")
                .containsEntry("password_hash", PASSWORD_HASH)
                .containsEntry("role", "USER");
    }

    @Test
    void persistsAnAdministratorRoleByName() {
        User savedAdministrator = userRepository.save(User.createAdministrator(
                new Username("grace"), new Email("grace@example.org"), new PasswordHash(PASSWORD_HASH)));
        entityManager.flush();

        assertThat(jdbcTemplate.queryForObject(
                "select role from users where id = ?", String.class, savedAdministrator.getId()))
                .isEqualTo("ADMIN");
    }

    @Test
    void rejectsADuplicateUsernameRegardlessOfCase() {
        userRepository.save(newUser("ada", "ada@example.org"));
        entityManager.flush();

        assertThatThrownBy(() -> {
            userRepository.save(newUser("ADA", "other@example.org"));
            entityManager.flush();
        })
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining(User.USERNAME_CONSTRAINT);
    }

    @Test
    void rejectsADuplicateEmailRegardlessOfCase() {
        userRepository.save(newUser("ada", "ada@example.org"));
        entityManager.flush();

        assertThatThrownBy(() -> {
            userRepository.save(newUser("grace", "ADA@example.org"));
            entityManager.flush();
        })
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining(User.EMAIL_CONSTRAINT);
    }

    @Test
    void persistsAChangedPasswordHash() {
        User savedUser = userRepository.save(newUser("ada", "ada@example.org"));

        savedUser.changePasswordHash(new PasswordHash(OTHER_PASSWORD_HASH));
        User updatedUser = userRepository.save(savedUser);
        entityManager.flush();
        entityManager.clear();

        User reloadedUser = userRepository.findById(updatedUser.getId()).orElseThrow();
        assertThat(reloadedUser.getPasswordHash()).isEqualTo(new PasswordHash(OTHER_PASSWORD_HASH));
    }

    private static User newUser(String username, String email) {
        return User.register(new Username(username), new Email(email), new PasswordHash(PASSWORD_HASH));
    }
}
