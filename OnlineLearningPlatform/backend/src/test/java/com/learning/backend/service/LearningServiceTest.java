package com.learning.backend.service;

import com.learning.backend.model.Course;
import com.learning.backend.model.UserAccount;
import com.learning.backend.repository.CourseRepository;
import com.learning.backend.repository.EnrollmentRepository;
import com.learning.backend.repository.LearningProgressRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LearningServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private LearningProgressRepository progressRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private LearningService learningService;

    @Test
    void enrollCreatesEnrollmentAndInitialProgress() {
        UserAccount user = mock(UserAccount.class);
        Course course = mock(Course.class);
        when(user.getUserId()).thenReturn(2);
        when(course.getCourseId()).thenReturn(9);
        when(course.getTitle()).thenReturn("React JS");
        when(courseRepository.findById(9)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByUserUserIdAndCourseCourseId(2, 9)).thenReturn(false);
        when(progressRepository.findFirstByUserUserIdAndCourseCourseId(2, 9))
                .thenReturn(Optional.empty());

        var result = learningService.enroll(user, 9);

        assertEquals(9, result.courseId());
        assertEquals("React JS", result.title());
        assertEquals(0, result.progressPercent());
        verify(enrollmentRepository).save(any());
        verify(progressRepository).save(any());
    }

    @Test
    void enrollRejectsDuplicateEnrollment() {
        UserAccount user = mock(UserAccount.class);
        Course course = mock(Course.class);
        when(user.getUserId()).thenReturn(2);
        when(courseRepository.findById(9)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByUserUserIdAndCourseCourseId(2, 9)).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> learningService.enroll(user, 9));

        assertEquals(409, exception.getStatusCode().value());
        verify(enrollmentRepository, never()).save(any());
    }
}
