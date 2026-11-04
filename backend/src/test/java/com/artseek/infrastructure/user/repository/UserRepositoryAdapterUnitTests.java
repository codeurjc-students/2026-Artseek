package com.artseek.infrastructure.user.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.artseek.domain.user.Email;
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

    @Mock
    private SpringDataUserRepository springDataUserRepository;

    @InjectMocks
    private UserRepositoryAdapter userRepositoryAdapter;

    @Test
    void delegatesFindByIdToSpringDataRepository() {
        Optional<User> expectedUser = Optional.of(newUser());
        when(springDataUserRepository.findById(1L)).thenReturn(expectedUser);

        Optional<User> result = userRepositoryAdapter.findById(1L);

        assertThat(result).isSameAs(expectedUser);
        verify(springDataUserRepository).findById(1L);
        verifyNoMoreInteractions(springDataUserRepository);
    }

    @Test
    void delegatesFindByUsernameToSpringDataRepository() {
        Optional<User> expectedUser = Optional.of(newUser());
        when(springDataUserRepository.findByUsername(USERNAME)).thenReturn(expectedUser);

        Optional<User> result = userRepositoryAdapter.findByUsername(USERNAME);

        assertThat(result).isSameAs(expectedUser);
        verify(springDataUserRepository).findByUsername(USERNAME);
        verifyNoMoreInteractions(springDataUserRepository);
    }

    @Test
    void delegatesFindByEmailToSpringDataRepository() {
        Optional<User> expectedUser = Optional.of(newUser());
        when(springDataUserRepository.findByEmail(EMAIL)).thenReturn(expectedUser);

        Optional<User> result = userRepositoryAdapter.findByEmail(EMAIL);

        assertThat(result).isSameAs(expectedUser);
        verify(springDataUserRepository).findByEmail(EMAIL);
        verifyNoMoreInteractions(springDataUserRepository);
    }

    @Test
    void delegatesExistsByUsernameToSpringDataRepository() {
        when(springDataUserRepository.existsByUsername(USERNAME)).thenReturn(true);

        assertThat(userRepositoryAdapter.existsByUsername(USERNAME)).isTrue();
        verify(springDataUserRepository).existsByUsername(USERNAME);
        verifyNoMoreInteractions(springDataUserRepository);
    }

    @Test
    void delegatesExistsByEmailToSpringDataRepository() {
        when(springDataUserRepository.existsByEmail(EMAIL)).thenReturn(false);

        assertThat(userRepositoryAdapter.existsByEmail(EMAIL)).isFalse();
        verify(springDataUserRepository).existsByEmail(EMAIL);
        verifyNoMoreInteractions(springDataUserRepository);
    }

    @Test
    void delegatesSaveToSpringDataRepository() {
        User user = newUser();
        when(springDataUserRepository.save(user)).thenReturn(user);

        User result = userRepositoryAdapter.save(user);

        assertThat(result).isSameAs(user);
        verify(springDataUserRepository).save(user);
        verifyNoMoreInteractions(springDataUserRepository);
    }

    private static User newUser() {
        return User.register("ada", "ada@example.org", PASSWORD_HASH);
    }
}
