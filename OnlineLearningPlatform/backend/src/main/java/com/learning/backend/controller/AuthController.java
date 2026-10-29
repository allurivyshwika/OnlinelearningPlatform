package com.learning.backend.controller;

import com.learning.backend.dto.LoginRequest;
import com.learning.backend.dto.ForgotPasswordRequest;
import com.learning.backend.dto.RegisterRequest;
import com.learning.backend.dto.ResetPasswordRequest;
import com.learning.backend.dto.UserResponse;
import com.learning.backend.model.UserAccount;
import com.learning.backend.service.AuthService;
import com.learning.backend.service.PasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    private final SecurityContextRepository securityContextRepository;

    public AuthController(
            AuthService authService,
            PasswordResetService passwordResetService,
            SecurityContextRepository securityContextRepository) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
        this.securityContextRepository = securityContextRepository;
    }

    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken csrfToken) {
        return Map.of("token", csrfToken.getToken());
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        UserAccount user = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(user));
    }

    @PostMapping("/password/forgot")
    public Map<String, String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return Map.of("message", passwordResetService.requestReset(request.email()));
    }

    @PostMapping("/password/reset")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.token(), request.password());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse) {
            try {
                Authentication authentication = authService.authenticate(request.email(), request.password());
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            if (servletRequest.getSession(false) != null) {
                servletRequest.changeSessionId();
            }
            securityContextRepository.saveContext(context, servletRequest, servletResponse);

            UserAccount user = (UserAccount) authentication.getPrincipal();
            return ResponseEntity.ok(toResponse(user));
        } catch (BadCredentialsException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Email or password is incorrect."));
        }
    }

    @GetMapping("/me")
    public UserResponse currentUser(Authentication authentication) {
        return toResponse((UserAccount) authentication.getPrincipal());
    }

    private UserResponse toResponse(UserAccount user) {
        return new UserResponse(user.getUserId(), user.getName(), user.getEmail(), user.getRole());
    }
}
