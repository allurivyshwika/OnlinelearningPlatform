package com.learning.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ManagedCourseRequest(
        @NotBlank @Size(max = 150) String title,
        @Size(max = 10000) String description,
        Integer categoryId) {
}
