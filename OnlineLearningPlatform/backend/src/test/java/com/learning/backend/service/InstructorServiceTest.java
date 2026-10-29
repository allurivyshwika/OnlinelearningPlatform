package com.learning.backend.service;

import com.learning.backend.dto.QuestionOptionRequest;
import com.learning.backend.dto.QuestionRequest;
import com.learning.backend.dto.QuizCreateRequest;
import com.learning.backend.model.Course;
import com.learning.backend.model.CourseInstructor;
import com.learning.backend.model.UserAccount;
import com.learning.backend.repository.CourseCategoryAssignmentRepository;
import com.learning.backend.repository.CourseCategoryRepository;
import com.learning.backend.repository.CourseInstructorRepository;
import com.learning.backend.repository.CourseRepository;
import com.learning.backend.repository.EnrollmentRepository;
import com.learning.backend.repository.QuizOptionRepository;
import com.learning.backend.repository.QuizQuestionRepository;
import com.learning.backend.repository.QuizRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InstructorServiceTest {

    @Mock
    private CourseInstructorRepository ownershipRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CourseCategoryRepository categoryRepository;

    @Mock
    private CourseCategoryAssignmentRepository categoryAssignmentRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private QuizQuestionRepository questionRepository;

    @Mock
    private QuizOptionRepository optionRepository;

    @InjectMocks
    private InstructorService instructorService;

    @Test
    void quizCreationRequiresExactlyOneCorrectAnswerPerQuestion() {
        UserAccount instructor = mock(UserAccount.class);
        CourseInstructor ownership = mock(CourseInstructor.class);
        Course course = mock(Course.class);
        when(instructor.getUserId()).thenReturn(4);
        when(ownershipRepository.findByCourseIdAndInstructorId(9, 4))
                .thenReturn(Optional.of(ownership));
        when(courseRepository.findById(9)).thenReturn(Optional.of(course));

        QuizCreateRequest request = new QuizCreateRequest(
                "Basics",
                List.of(new QuestionRequest(
                        "Which is correct?",
                        List.of(
                                new QuestionOptionRequest("First", true),
                                new QuestionOptionRequest("Second", true)))));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> instructorService.createQuiz(instructor, 9, request));

        assertEquals(400, exception.getStatusCode().value());
        verify(quizRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
