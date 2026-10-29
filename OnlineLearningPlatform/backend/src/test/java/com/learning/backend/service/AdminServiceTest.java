package com.learning.backend.service;

import com.learning.backend.dto.AdminCourseResponse;
import com.learning.backend.dto.ManagedCourseRequest;
import com.learning.backend.dto.QuestionOptionRequest;
import com.learning.backend.dto.QuestionRequest;
import com.learning.backend.dto.QuizCreateRequest;
import com.learning.backend.dto.RoleUpdateRequest;
import com.learning.backend.model.Course;
import com.learning.backend.model.UserAccount;
import com.learning.backend.repository.CourseCategoryAssignmentRepository;
import com.learning.backend.repository.CourseCategoryRepository;
import com.learning.backend.repository.CourseInstructorRepository;
import com.learning.backend.repository.CourseRepository;
import com.learning.backend.repository.EnrollmentRepository;
import com.learning.backend.repository.QuizOptionRepository;
import com.learning.backend.repository.QuizQuestionRepository;
import com.learning.backend.repository.QuizRepository;
import com.learning.backend.repository.UserAccountRepository;
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
class AdminServiceTest {

    @Mock
    private UserAccountRepository userRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CourseCategoryRepository categoryRepository;

    @Mock
    private CourseCategoryAssignmentRepository categoryAssignmentRepository;

    @Mock
    private CourseInstructorRepository courseInstructorRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private QuizQuestionRepository questionRepository;

    @Mock
    private QuizOptionRepository optionRepository;

    @InjectMocks
    private AdminService adminService;

    @Test
    void administratorCannotRemoveTheirOwnAdminRole() {
        UserAccount administrator = mock(UserAccount.class);
        when(administrator.getUserId()).thenReturn(7);
        when(userRepository.findById(7)).thenReturn(Optional.of(administrator));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> adminService.updateRole(7, new RoleUpdateRequest("STUDENT"), administrator));

        assertEquals(409, exception.getStatusCode().value());
        verify(administrator, never()).setRole("STUDENT");
    }

    @Test
    void quizCreationRejectsQuestionsWithoutExactlyOneCorrectOption() {
        when(courseRepository.existsById(12)).thenReturn(true);
        QuizCreateRequest request = new QuizCreateRequest(
                "Course quiz",
                List.of(new QuestionRequest(
                        "Which answer is correct?",
                        List.of(
                                new QuestionOptionRequest("First", true),
                                new QuestionOptionRequest("Second", true)))));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> adminService.createQuiz(12, request));

        assertEquals(400, exception.getStatusCode().value());
        verify(quizRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void adminCourseListIncludesActualEnrollmentCount() {
        Course course = mock(Course.class);
        when(course.getCourseId()).thenReturn(12);
        when(course.getTitle()).thenReturn("Java");
        when(course.getInstructor()).thenReturn("Ravi");
        when(courseRepository.findAll()).thenReturn(List.of(course));
        when(courseInstructorRepository.findById(12)).thenReturn(Optional.empty());
        when(enrollmentRepository.countByCourseCourseId(12)).thenReturn(5L);

        List<AdminCourseResponse> result = adminService.courses();

        assertEquals(1, result.size());
        assertEquals(5L, result.get(0).enrollmentCount());
    }

    @Test
    void adminCanCreateCourseWithoutAssigningInstructor() {
        when(courseRepository.save(org.mockito.ArgumentMatchers.any(Course.class)))
                .thenAnswer(invocation -> {
                    Course saved = invocation.getArgument(0);
                    saved.setCourseId(21);
                    return saved;
                });

        AdminCourseResponse result = adminService.createCourse(
                new ManagedCourseRequest("  Intro to Python  ", "  Basics  ", null));

        assertEquals(21, result.courseId());
        assertEquals("Intro to Python", result.title());
        assertEquals(0L, result.enrollmentCount());
        verify(courseRepository).save(org.mockito.ArgumentMatchers.any(Course.class));
    }
}
