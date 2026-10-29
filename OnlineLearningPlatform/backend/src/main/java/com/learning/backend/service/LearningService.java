package com.learning.backend.service;

import com.learning.backend.dto.CourseProgressResponse;
import com.learning.backend.dto.ProgressRequest;
import com.learning.backend.model.Course;
import com.learning.backend.model.Enrollment;
import com.learning.backend.model.LearningProgress;
import com.learning.backend.model.UserAccount;
import com.learning.backend.repository.CourseRepository;
import com.learning.backend.repository.EnrollmentRepository;
import com.learning.backend.repository.LearningProgressRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class LearningService {

    private final EnrollmentRepository enrollmentRepository;
    private final LearningProgressRepository progressRepository;
    private final CourseRepository courseRepository;

    public LearningService(
            EnrollmentRepository enrollmentRepository,
            LearningProgressRepository progressRepository,
            CourseRepository courseRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.progressRepository = progressRepository;
        this.courseRepository = courseRepository;
    }

    @Transactional(readOnly = true)
    public List<CourseProgressResponse> dashboard(UserAccount user) {
        return enrollmentRepository.findByUserUserIdOrderByEnrolledAtDesc(user.getUserId()).stream()
                .map(enrollment -> toResponse(enrollment.getCourse(), user.getUserId()))
                .toList();
    }

    @Transactional
    public CourseProgressResponse enroll(UserAccount user, Integer courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found."));
        if (enrollmentRepository.existsByUserUserIdAndCourseCourseId(user.getUserId(), courseId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You are already enrolled in this course.");
        }

        enrollmentRepository.save(new Enrollment(user, course));
        progressRepository.findFirstByUserUserIdAndCourseCourseId(user.getUserId(), courseId)
                .orElseGet(() -> progressRepository.save(new LearningProgress(user, course)));
        return toResponse(course, user.getUserId());
    }

    @Transactional
    public CourseProgressResponse updateProgress(UserAccount user, Integer courseId, ProgressRequest request) {
        if (!enrollmentRepository.existsByUserUserIdAndCourseCourseId(user.getUserId(), courseId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Enroll in this course before updating progress.");
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found."));
        LearningProgress progress = progressRepository
                .findFirstByUserUserIdAndCourseCourseId(user.getUserId(), courseId)
                .orElseGet(() -> new LearningProgress(user, course));
        progress.setProgressPercent(request.progressPercent());
        progressRepository.save(progress);
        return toResponse(course, user.getUserId());
    }

    private CourseProgressResponse toResponse(Course course, Integer userId) {
        int progress = progressRepository
                .findFirstByUserUserIdAndCourseCourseId(userId, course.getCourseId())
                .map(LearningProgress::getProgressPercent)
                .orElse(0);
        return new CourseProgressResponse(
                course.getCourseId(),
                course.getTitle(),
                course.getDescription(),
                course.getInstructor(),
                progress);
    }
}
