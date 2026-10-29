package com.learning.backend.service;

import com.learning.backend.model.PasswordResetToken;
import com.learning.backend.repository.PasswordResetTokenRepository;
import com.learning.backend.repository.UserAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;

@Service
public class PasswordResetService {

    private static final Logger logger = LoggerFactory.getLogger(PasswordResetService.class);
    private static final String GENERIC_REQUEST_MESSAGE =
            "If an account exists for that email, a password reset link will be sent.";

    private final PasswordResetTokenRepository tokenRepository;
    private final UserAccountRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final SecureRandom secureRandom = new SecureRandom();
    private final String mailHost;
    private final String mailUsername;
    private final String mailPassword;
    private final String mailFrom;
    private final String frontendUrl;
    private final long tokenMinutes;

    public PasswordResetService(
            PasswordResetTokenRepository tokenRepository,
            UserAccountRepository userRepository,
            PasswordEncoder passwordEncoder,
            JavaMailSender mailSender,
            @Value("${spring.mail.host:}") String mailHost,
            @Value("${spring.mail.username:}") String mailUsername,
            @Value("${spring.mail.password:}") String mailPassword,
            @Value("${app.mail.from:}") String mailFrom,
            @Value("${app.frontend-url}") String frontendUrl,
            @Value("${app.password-reset.token-minutes:30}") long tokenMinutes) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
        this.mailHost = mailHost;
        this.mailUsername = mailUsername;
        this.mailPassword = mailPassword;
        this.mailFrom = mailFrom;
        this.frontendUrl = frontendUrl;
        this.tokenMinutes = tokenMinutes;
    }

    @Transactional
    public String requestReset(String email) {
        requireMailConfiguration();
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        var user = userRepository.findByEmail(normalizedEmail);
        if (user.isEmpty()) {
            return GENERIC_REQUEST_MESSAGE;
        }

        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        String tokenHash = hashToken(rawToken);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(tokenMinutes);

        tokenRepository.deleteAllByUserId(user.get().getUserId());
        tokenRepository.save(new PasswordResetToken(tokenHash, user.get().getUserId(), expiresAt));

        String resetUrl = UriComponentsBuilder.fromUriString(frontendUrl)
                .path("/reset-password")
                .queryParam("token", rawToken)
                .build()
                .toUriString();
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(StringUtils.hasText(mailFrom) ? mailFrom : mailUsername);
        message.setTo(user.get().getEmail());
        message.setSubject("Reset your LearnSpace password");
        message.setText("""
                Hello %s,

                We received a request to reset the password for your LearnSpace account.
                Use the link below within %d minutes to choose a new password:

                %s

                If you did not request this, you can ignore this email. Your password will not change.
                """.formatted(user.get().getName(), tokenMinutes, resetUrl));
        try {
            mailSender.send(message);
        } catch (MailException exception) {
            logger.error("Could not send password reset email for user id {}.", user.get().getUserId(), exception);
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Password reset email could not be sent. Check the email server settings and try again.",
                    exception);
        }
        return GENERIC_REQUEST_MESSAGE;
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        String tokenHash = hashToken(rawToken);
        LocalDateTime now = LocalDateTime.now();
        PasswordResetToken resetToken = tokenRepository
                .findByTokenHashAndUsedAtIsNullAndExpiresAtAfter(tokenHash, now)
                .orElseThrow(() -> invalidToken());

        if (tokenRepository.consumeIfActive(tokenHash, now) != 1) {
            throw invalidToken();
        }

        var user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> invalidToken());
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        tokenRepository.deleteAllByUserId(user.getUserId());
    }

    private void requireMailConfiguration() {
        if (!StringUtils.hasText(mailHost)
                || !StringUtils.hasText(mailUsername)
                || !StringUtils.hasText(mailPassword)
                || (!StringUtils.hasText(mailFrom) && !StringUtils.hasText(mailUsername))) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Password reset email is not configured. Set the SMTP environment variables and restart the backend.");
        }
    }

    private String hashToken(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available.", exception);
        }
    }

    private ResponseStatusException invalidToken() {
        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "This password reset link is invalid or has expired.");
    }
}
