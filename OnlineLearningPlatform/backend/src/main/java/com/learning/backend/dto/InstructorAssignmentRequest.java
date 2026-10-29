package com.learning.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record InstructorAssignmentRequest(@NotNull @Positive Integer instructorId) {
}
