package com.example.learning.controller;

import com.example.learning.model.AppUser;
import com.example.learning.repository.UserRepository;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository users;
    public AuthController(UserRepository users) { this.users = users; }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String name = body.getOrDefault("name", "").trim();
        String email = body.getOrDefault("email", "").trim().toLowerCase();
        String password = body.getOrDefault("password", "");
        if (name.isBlank() || email.isBlank() || password.length() < 6)
            return ResponseEntity.badRequest().body(Map.of("message", "Enter name, email, and a password of at least 6 characters."));
        if (users.findByEmail(email).isPresent())
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Email is already registered."));
        AppUser user = new AppUser();
        user.setName(name); user.setEmail(email);
        // Demo only: never store plain-text passwords in a real application.
        user.setPasswordHash(password);
        users.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", user.getId(), "name", name, "email", email));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email = body.getOrDefault("email", "").trim().toLowerCase();
        String password = body.getOrDefault("password", "");
        return users.findByEmail(email)
            .filter(u -> u.getPasswordHash().equals(password))
            .<ResponseEntity<?>>map(u -> ResponseEntity.ok(Map.of("id", u.getId(), "name", u.getName(), "email", u.getEmail())))
            .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid email or password.")));
    }
}
