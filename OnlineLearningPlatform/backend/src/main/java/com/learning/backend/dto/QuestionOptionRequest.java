package com.learning.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record QuestionOptionRequest(
        @NotBlank @Size(max = 255) String text,
        boolean correct) {
}
