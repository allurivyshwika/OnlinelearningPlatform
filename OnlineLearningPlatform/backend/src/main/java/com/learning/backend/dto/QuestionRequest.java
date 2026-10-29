package com.learning.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record QuestionRequest(
        @NotBlank @Size(max = 2000) String question,
        @NotNull @Valid @Size(min = 2, max = 8) List<QuestionOptionRequest> options) {
}
