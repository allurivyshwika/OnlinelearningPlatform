package com.learning.backend.service;

import com.learning.backend.model.Course;
import com.learning.backend.repository.CourseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private CourseService courseService;

    @Test
    void findByIdReturnsTheCourse() {
        Course course = mock(Course.class);
        when(courseRepository.findById(7)).thenReturn(Optional.of(course));

        assertSame(course, courseService.findById(7));
    }

    @Test
    void findByIdReturnsNotFoundForUnknownCourse() {
        when(courseRepository.findById(99)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> courseService.findById(99));

        assertEquals(404, exception.getStatusCode().value());
    }
}
