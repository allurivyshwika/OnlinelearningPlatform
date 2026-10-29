package com.learning.backend.dto;

import java.util.Map;

public record QuizAttemptRequest(Map<Integer, Integer> answers) {
}
