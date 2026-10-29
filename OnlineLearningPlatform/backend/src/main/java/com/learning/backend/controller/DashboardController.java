package com.learning.backend.controller;

import com.learning.backend.dto.CourseProgressResponse;
import com.learning.backend.dto.ProgressRequest;
import com.learning.backend.model.UserAccount;
import com.learning.backend.service.LearningService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DashboardController {

    private final LearningService learningService;

    public DashboardController(LearningService learningService) {
        this.learningService = learningService;
    }

    @GetMapping("/api/dashboard")
    public List<CourseProgressResponse> dashboard(@AuthenticationPrincipal UserAccount user) {
        return learningService.dashboard(user);
    }

    @PostMapping("/api/enrollments/{courseId}")
    public ResponseEntity<CourseProgressResponse> enroll(
            @AuthenticationPrincipal UserAccount user,
            @PathVariable Integer courseId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(learningService.enroll(user, courseId));
    }

    @PutMapping("/api/enrollments/{courseId}/progress")
    public CourseProgressResponse updateProgress(
            @AuthenticationPrincipal UserAccount user,
            @PathVariable Integer courseId,
            @Valid @RequestBody ProgressRequest request) {
        return learningService.updateProgress(user, courseId, request);
    }
}
