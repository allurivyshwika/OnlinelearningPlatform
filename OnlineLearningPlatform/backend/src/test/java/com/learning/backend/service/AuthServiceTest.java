package com.learning.backend.service;

import com.learning.backend.dto.RegisterRequest;
import com.learning.backend.model.UserAccount;
import com.learning.backend.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserAccountRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerNormalizesEmailAndStoresEncodedPassword() {
        when(userRepository.existsByEmail("learner@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secure-pass")).thenReturn("bcrypt-hash");

        authService.register(new RegisterRequest(" Learner ", "Learner@Example.com", "secure-pass"));

        ArgumentCaptor<UserAccount> account = ArgumentCaptor.forClass(UserAccount.class);
        verify(userRepository).save(account.capture());
        assertEquals("Learner", account.getValue().getName());
        assertEquals("learner@example.com", account.getValue().getEmail());
        assertEquals("bcrypt-hash", account.getValue().getPassword());
    }

    @Test
    void registerRejectsAnExistingEmail() {
        when(userRepository.existsByEmail("learner@example.com")).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.register(
                        new RegisterRequest("Learner", "Learner@Example.com", "secure-pass")));

        assertEquals(409, exception.getStatusCode().value());
    }
}
