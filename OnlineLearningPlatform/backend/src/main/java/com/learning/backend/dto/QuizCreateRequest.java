package com.learning.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record QuizCreateRequest(
        @NotBlank @Size(max = 150) String title,
        @NotNull @Valid @Size(min = 1, max = 30) List<QuestionRequest> questions) {
}
