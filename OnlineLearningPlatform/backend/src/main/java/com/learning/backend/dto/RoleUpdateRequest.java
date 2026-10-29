package com.learning.backend.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;

public record RoleUpdateRequest(
        @NotBlank @Pattern(regexp = "STUDENT|INSTRUCTOR|ADMIN") String role) {
}
