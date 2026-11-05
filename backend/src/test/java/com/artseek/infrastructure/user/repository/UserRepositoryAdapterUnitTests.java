package com.artseek.infrastructure.user.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.artseek.domain.user.Email;
import com.artseek.domain.user.PasswordHash;
import com.artseek.domain.user.Role;
import com.artseek.domain.user.User;
import com.artseek.domain.user.Username;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserRepositoryAdapterUnitTests {

    private static final Username USERNAME = new Username("ada");
    private static final Email EMAIL = new Email("ada@example.org");
    private static final String PASSWORD_HASH =
            "$argon2id$v=19$m=19456,t=2,p=1$c2FsdHNhbHRzYWx0c2FsdA$aGFzaGhhc2hoYXNoaGFzaGhhc2hoYXNoaGFzaGhhc2g";
    private static final UserPersistenceModel ADA =
            new UserPersistenceModel(1L, "ada", "ada@example.org", PASSWORD_HASH, "USER", null, null);

    @Mock
    private SpringDataUserRepository springDataUserRepository;

    @InjectMocks
    private UserRepositoryAdapter userRepositoryAdapter;

    @Test
    void mapsTheRowFoundById() {
        when(springDataUserRepository.findById(1L)).thenReturn(Optional.of(ADA));

        Optional<User> result = userRepositoryAdapter.findById(1L);

        assertThat(result).get().satisfies(user -> {
            assertThat(user.getId()).isEqualTo(1L);
            assertThat(user.getUsername()).isEqualTo(USERNAME);
            assertThat(user.getEmail()).isEqualTo(EMAIL);
            assertThat(user.getPasswordHash()).isEqualTo(new PasswordHash(PASSWORD_HASH));
            assertThat(user.getRole()).isEqualTo(Role.USER);
        });
        verify(springDataUserRepository).findById(1L);
        verifyNoMoreInteractions(springDataUserRepository);
    }

    @Test
    void findsByTheStoredUsernameValue() {
        when(springDataUserRepository.findByUsername("ada")).thenReturn(Optional.of(ADA));

        Optional<User> result = userRepositoryAdapter.findByUsername(USERNAME);

        assertThat(result).get().extracting(User::getId).isEqualTo(1L);
        verify(springDataUserRepository).findByUsername("ada");
        verifyNoMoreInteractions(springDataUserRepository);
    }

    @Test
    void findsByTheStoredEmailValue() {
        when(springDataUserRepository.findByEmail("ada@example.org")).thenReturn(Optional.empty());

        Optional<User> result = userRepositoryAdapter.findByEmail(EMAIL);

        assertThat(result).isEmpty();
        verify(springDataUserRepository).findByEmail("ada@example.org");
        verifyNoMoreInteractions(springDataUserRepository);
    }

    @Test
    void checksUsernameExistenceByTheStoredValue() {
        when(springDataUserRepository.existsByUsername("ada")).thenReturn(true);

        assertThat(userRepositoryAdapter.existsByUsername(USERNAME)).isTrue();
        verify(springDataUserRepository).existsByUsername("ada");
        verifyNoMoreInteractions(springDataUserRepository);
    }

    @Test
    void checksEmailExistenceByTheStoredValue() {
        when(springDataUserRepository.existsByEmail("ada@example.org")).thenReturn(false);

        assertThat(userRepositoryAdapter.existsByEmail(EMAIL)).isFalse();
        verify(springDataUserRepository).existsByEmail("ada@example.org");
        verifyNoMoreInteractions(springDataUserRepository);
    }

    @Test
    void savesTheMappedRowAndReturnsTheSavedUser() {
        UserPersistenceModel unsaved =
                new UserPersistenceModel(null, "ada", "ada@example.org", PASSWORD_HASH, "USER", null, null);
        when(springDataUserRepository.save(unsaved)).thenReturn(ADA);

        User result = userRepositoryAdapter.save(User.register(USERNAME, EMAIL, new PasswordHash(PASSWORD_HASH)));

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo(USERNAME);
        verify(springDataUserRepository).save(unsaved);
        verifyNoMoreInteractions(springDataUserRepository);
    }
}
