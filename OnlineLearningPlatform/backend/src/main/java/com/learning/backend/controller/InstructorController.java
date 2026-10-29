package com.learning.backend.controller;

import com.learning.backend.dto.InstructorCourseResponse;
import com.learning.backend.dto.ManagedCourseRequest;
import com.learning.backend.dto.QuizCreateRequest;
import com.learning.backend.model.Quiz;
import com.learning.backend.model.UserAccount;
import com.learning.backend.service.InstructorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/instructor")
@PreAuthorize("hasRole('INSTRUCTOR')")
public class InstructorController {

    private final InstructorService instructorService;

    public InstructorController(InstructorService instructorService) {
        this.instructorService = instructorService;
    }

    @GetMapping("/dashboard")
    public List<InstructorCourseResponse> dashboard(@AuthenticationPrincipal UserAccount instructor) {
        return instructorService.dashboard(instructor);
    }

    @PostMapping("/courses")
    public ResponseEntity<InstructorCourseResponse> createCourse(
            @AuthenticationPrincipal UserAccount instructor,
            @Valid @RequestBody ManagedCourseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(instructorService.createCourse(instructor, request));
    }

    @PutMapping("/courses/{courseId}")
    public InstructorCourseResponse updateCourse(
            @AuthenticationPrincipal UserAccount instructor,
            @PathVariable Integer courseId,
            @Valid @RequestBody ManagedCourseRequest request) {
        return instructorService.updateCourse(instructor, courseId, request);
    }

    @DeleteMapping("/courses/{courseId}")
    public ResponseEntity<Void> deleteCourse(
            @AuthenticationPrincipal UserAccount instructor,
            @PathVariable Integer courseId) {
        instructorService.deleteCourse(instructor, courseId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/courses/{courseId}/quizzes")
    public ResponseEntity<Quiz> createQuiz(
            @AuthenticationPrincipal UserAccount instructor,
            @PathVariable Integer courseId,
            @Valid @RequestBody QuizCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(instructorService.createQuiz(instructor, courseId, request));
    }
}
