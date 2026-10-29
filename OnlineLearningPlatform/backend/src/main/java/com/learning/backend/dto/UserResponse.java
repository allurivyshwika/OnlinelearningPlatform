package com.learning.backend.dto;

public record UserResponse(Integer userId, String name, String email, String role) {
}
