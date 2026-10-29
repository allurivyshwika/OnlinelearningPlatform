package com.learning.backend.dto;

public record ManagedUserResponse(Integer userId, String name, String email, String role) {
}
