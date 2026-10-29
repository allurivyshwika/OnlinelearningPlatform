package com.learning.backend.service;

import com.learning.backend.model.PasswordResetToken;
import com.learning.backend.model.UserAccount;
import com.learning.backend.repository.PasswordResetTokenRepository;
import com.learning.backend.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private UserAccountRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private UserAccount user;

    private PasswordResetService passwordResetService;

    @BeforeEach
    void configureMail() {
        passwordResetService = new PasswordResetService(
                tokenRepository,
                userRepository,
                passwordEncoder,
                mailSender,
                "smtp.example.com",
                "sender@example.com",
                "smtp-password",
                "",
                "http://localhost:5173",
                30);
    }

    @Test
    void resetRequestDoesNotRevealWhetherEmailExists() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        String response = passwordResetService.requestReset(" Missing@Example.com ");

        assertEquals(
                "If an account exists for that email, a password reset link will be sent.",
                response);
    }

    @Test
    void resetLinkUsesHashedStoredTokenAndCanChangePasswordOnce() throws Exception {
        when(userRepository.findByEmail("learner@example.com")).thenReturn(Optional.of(user));
        when(user.getUserId()).thenReturn(17);
        when(user.getEmail()).thenReturn("learner@example.com");
        when(user.getName()).thenReturn("Learner");
        ArgumentCaptor<PasswordResetToken> tokenCaptor =
                ArgumentCaptor.forClass(PasswordResetToken.class);

        passwordResetService.requestReset("Learner@Example.com");

        verify(tokenRepository).save(tokenCaptor.capture());
        ArgumentCaptor<SimpleMailMessage> mailCaptor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(mailCaptor.capture());
        Matcher matcher = Pattern.compile("token=([A-Za-z0-9_-]+)")
                .matcher(mailCaptor.getValue().getText());
        assertTrue(matcher.find());
        String rawToken = matcher.group(1);
        String expectedHash = HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256").digest(rawToken.getBytes(StandardCharsets.UTF_8)));
        assertEquals(expectedHash, tokenCaptor.getValue().getTokenHash());

        when(tokenRepository.findByTokenHashAndUsedAtIsNullAndExpiresAtAfter(
                eq(expectedHash), any(LocalDateTime.class))).thenReturn(Optional.of(tokenCaptor.getValue()));
        when(tokenRepository.consumeIfActive(any(String.class), any(LocalDateTime.class))).thenReturn(1);
        when(userRepository.findById(17)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("new-secure-password")).thenReturn("bcrypt-hash");

        passwordResetService.resetPassword(rawToken, "new-secure-password");

        verify(user).setPasswordHash("bcrypt-hash");
        verify(userRepository).save(user);
        verify(tokenRepository, times(2)).deleteAllByUserId(17);
    }
}
