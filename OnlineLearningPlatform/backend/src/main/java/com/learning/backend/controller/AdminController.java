package com.learning.backend.controller;

import com.learning.backend.dto.AdminStatisticsResponse;
import com.learning.backend.dto.AdminCourseResponse;
import com.learning.backend.dto.CategoryRequest;
import com.learning.backend.dto.CategoryResponse;
import com.learning.backend.dto.InstructorAssignmentRequest;
import com.learning.backend.dto.ManagedCourseRequest;
import com.learning.backend.dto.ManagedUserResponse;
import com.learning.backend.dto.QuizCreateRequest;
import com.learning.backend.dto.RoleUpdateRequest;
import com.learning.backend.model.Quiz;
import com.learning.backend.model.UserAccount;
import com.learning.backend.service.AdminService;
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
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    public AdminStatisticsResponse dashboard() {
        return adminService.statistics();
    }

    @GetMapping("/users")
    public List<ManagedUserResponse> users() {
        return adminService.users();
    }

    @GetMapping("/courses")
    public List<AdminCourseResponse> courses() {
        return adminService.courses();
    }

    @PostMapping("/courses")
    public ResponseEntity<AdminCourseResponse> createCourse(
            @Valid @RequestBody ManagedCourseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createCourse(request));
    }

    @PostMapping("/courses/{courseId}/quizzes")
    public ResponseEntity<Quiz> createQuiz(
            @PathVariable Integer courseId,
            @Valid @RequestBody QuizCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminService.createQuiz(courseId, request));
    }

    @PutMapping("/courses/{courseId}/instructor")
    public AdminCourseResponse assignInstructor(
            @PathVariable Integer courseId,
            @Valid @RequestBody InstructorAssignmentRequest request) {
        return adminService.assignInstructor(courseId, request);
    }

    @DeleteMapping("/courses/{courseId}")
    public void deleteCourse(@PathVariable Integer courseId) {
        adminService.deleteCourse(courseId);
    }

    @PutMapping("/users/{userId}/role")
    public ManagedUserResponse updateRole(
            @AuthenticationPrincipal UserAccount actor,
            @PathVariable Integer userId,
            @Valid @RequestBody RoleUpdateRequest request) {
        return adminService.updateRole(userId, request, actor);
    }

    @GetMapping("/categories")
    public List<CategoryResponse> categories() {
        return adminService.categories();
    }

    @PostMapping("/categories")
    public CategoryResponse createCategory(@Valid @RequestBody CategoryRequest request) {
        return adminService.createCategory(request);
    }

    @DeleteMapping("/categories/{categoryId}")
    public void deleteCategory(@PathVariable Integer categoryId) {
        adminService.deleteCategory(categoryId);
    }
}
