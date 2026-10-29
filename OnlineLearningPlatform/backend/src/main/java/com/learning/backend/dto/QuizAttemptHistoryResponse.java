package com.learning.backend.dto;

import java.time.LocalDateTime;

public record QuizAttemptHistoryResponse(
        Integer attemptId,
        String courseTitle,
        String quizTitle,
        Integer score,
        Integer totalQuestions,
        LocalDateTime attemptedAt) {
}
