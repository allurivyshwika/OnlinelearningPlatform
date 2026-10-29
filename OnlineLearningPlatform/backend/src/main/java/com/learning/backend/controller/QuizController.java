package com.learning.backend.controller;

import com.learning.backend.dto.QuizAttemptHistoryResponse;
import com.learning.backend.dto.QuizAttemptRequest;
import com.learning.backend.dto.QuizAttemptResponse;
import com.learning.backend.dto.QuizResponse;
import com.learning.backend.model.UserAccount;
import com.learning.backend.service.QuizService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping("/api/courses/{courseId}/quizzes")
    public List<QuizResponse> quizzes(
            @AuthenticationPrincipal UserAccount user,
            @PathVariable Integer courseId) {
        return quizService.findForCourse(user, courseId);
    }

    @PostMapping("/api/quizzes/{quizId}/attempts")
    public QuizAttemptResponse submit(
            @AuthenticationPrincipal UserAccount user,
            @PathVariable Integer quizId,
            @Valid @RequestBody QuizAttemptRequest request) {
        return quizService.submit(user, quizId, request);
    }

    @GetMapping("/api/quiz-attempts")
    public List<QuizAttemptHistoryResponse> history(@AuthenticationPrincipal UserAccount user) {
        return quizService.history(user);
    }
}
