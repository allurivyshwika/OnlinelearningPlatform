package com.learning.backend.dto;

import java.util.List;

public record QuizQuestionResponse(Integer questionId, String question, List<QuizOptionResponse> options) {
}
