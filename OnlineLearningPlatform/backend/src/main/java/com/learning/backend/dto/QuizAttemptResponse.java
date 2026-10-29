package com.learning.backend.dto;

public record QuizAttemptResponse(Integer score, Integer totalQuestions, Integer progressPercent) {
}
