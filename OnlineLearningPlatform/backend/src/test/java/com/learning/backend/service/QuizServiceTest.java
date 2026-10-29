package com.learning.backend.service;

import com.learning.backend.dto.QuizAttemptRequest;
import com.learning.backend.dto.QuizAttemptResponse;
import com.learning.backend.model.Course;
import com.learning.backend.model.LearningProgress;
import com.learning.backend.model.Quiz;
import com.learning.backend.model.QuizOption;
import com.learning.backend.model.QuizQuestion;
import com.learning.backend.model.UserAccount;
import com.learning.backend.repository.CourseRepository;
import com.learning.backend.repository.EnrollmentRepository;
import com.learning.backend.repository.LearningProgressRepository;
import com.learning.backend.repository.QuizAttemptRepository;
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
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuizServiceTest {

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private QuizQuestionRepository questionRepository;

    @Mock
    private QuizOptionRepository optionRepository;

    @Mock
    private QuizAttemptRepository attemptRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private LearningProgressRepository progressRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private QuizService quizService;

    @Test
    void submitGradesAnswersAndUpdatesEnrolledLearnersProgress() {
        UserAccount user = mock(UserAccount.class);
        Quiz quiz = mock(Quiz.class);
        QuizQuestion question = mock(QuizQuestion.class);
        QuizOption option = mock(QuizOption.class);
        Course course = mock(Course.class);
        when(user.getUserId()).thenReturn(3);
        when(quiz.getCourseId()).thenReturn(8);
        when(quizRepository.findById(5)).thenReturn(Optional.of(quiz));
        when(enrollmentRepository.existsByUserUserIdAndCourseCourseId(3, 8)).thenReturn(true);
        when(questionRepository.findAllByQuizIdOrderByQuestionOrder(5)).thenReturn(List.of(question));
        when(question.getQuestionId()).thenReturn(12);
        when(optionRepository.findById(17)).thenReturn(Optional.of(option));
        when(option.getQuestionId()).thenReturn(12);
        when(option.isCorrect()).thenReturn(true);
        when(courseRepository.findById(8)).thenReturn(Optional.of(course));
        when(course.getCourseId()).thenReturn(8);
        when(progressRepository.findFirstByUserUserIdAndCourseCourseId(3, 8)).thenReturn(Optional.empty());

        QuizAttemptResponse result = quizService.submit(
                user, 5, new QuizAttemptRequest(Map.of(12, 17)));

        assertEquals(new QuizAttemptResponse(1, 1, 100), result);
        verify(attemptRepository).save(any());
        verify(progressRepository).save(any(LearningProgress.class));
    }

    @Test
    void submitRejectsOptionsFromAnotherQuestion() {
        UserAccount user = mock(UserAccount.class);
        Quiz quiz = mock(Quiz.class);
        QuizQuestion question = mock(QuizQuestion.class);
        QuizOption option = mock(QuizOption.class);
        when(user.getUserId()).thenReturn(3);
        when(quiz.getCourseId()).thenReturn(8);
        when(quizRepository.findById(5)).thenReturn(Optional.of(quiz));
        when(enrollmentRepository.existsByUserUserIdAndCourseCourseId(3, 8)).thenReturn(true);
        when(questionRepository.findAllByQuizIdOrderByQuestionOrder(5)).thenReturn(List.of(question));
        when(question.getQuestionId()).thenReturn(12);
        when(optionRepository.findById(17)).thenReturn(Optional.of(option));
        when(option.getQuestionId()).thenReturn(99);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> quizService.submit(user, 5, new QuizAttemptRequest(Map.of(12, 17))));

        assertEquals(400, exception.getStatusCode().value());
        verify(attemptRepository, never()).save(any());
    }
}
