package com.learning.backend.dto;

import java.util.List;

public record QuizResponse(Integer quizId, String title, List<QuizQuestionResponse> questions) {
}
