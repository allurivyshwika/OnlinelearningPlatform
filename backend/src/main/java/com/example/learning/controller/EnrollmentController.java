package com.example.learning.controller;

import com.example.learning.model.Course;
import com.example.learning.model.Enrollment;
import com.example.learning.repository.CourseRepository;
import com.example.learning.repository.EnrollmentRepository;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {
    private final EnrollmentRepository enrollments;
    private final CourseRepository courses;
    public EnrollmentController(EnrollmentRepository enrollments, CourseRepository courses) {
        this.enrollments = enrollments; this.courses = courses;
    }

    @PostMapping
    public ResponseEntity<?> enroll(@RequestBody Map<String, Long> body) {
        Long userId = body.get("userId"), courseId = body.get("courseId");
        if (userId == null || courseId == null || !courses.existsById(courseId))
            return ResponseEntity.badRequest().body(Map.of("message", "Valid userId and courseId are required."));
        Optional<Enrollment> existing = enrollments.findByUserIdAndCourseId(userId, courseId);
        if (existing.isPresent()) return ResponseEntity.ok(existing.get());
        Enrollment enrollment = new Enrollment();
        enrollment.setUserId(userId); enrollment.setCourseId(courseId);
        return ResponseEntity.status(HttpStatus.CREATED).body(enrollments.save(enrollment));
    }

    @GetMapping("/user/{userId}")
    public List<Map<String, Object>> byUser(@PathVariable Long userId) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Enrollment e : enrollments.findByUserId(userId)) {
            courses.findById(e.getCourseId()).ifPresent(c -> {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("enrollmentId", e.getId()); row.put("progressPercent", e.getProgressPercent());
                row.put("course", c); result.add(row);
            });
        }
        return result;
    }

    @PutMapping("/{id}/progress")
    public ResponseEntity<?> progress(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        return enrollments.findById(id).<ResponseEntity<?>>map(e -> {
            int value = Math.max(0, Math.min(100, body.getOrDefault("progressPercent", 0)));
            e.setProgressPercent(value);
            return ResponseEntity.ok(enrollments.save(e));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
