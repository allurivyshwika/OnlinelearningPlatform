package com.learning.backend.service;

import com.learning.backend.dto.InstructorCourseResponse;
import com.learning.backend.dto.ManagedCourseRequest;
import com.learning.backend.dto.QuestionRequest;
import com.learning.backend.dto.QuizCreateRequest;
import com.learning.backend.model.Course;
import com.learning.backend.model.CourseCategoryAssignment;
import com.learning.backend.model.CourseInstructor;
import com.learning.backend.repository.CourseCategoryAssignmentRepository;
import com.learning.backend.repository.CourseCategoryRepository;
import com.learning.backend.model.Quiz;
import com.learning.backend.model.QuizOption;
import com.learning.backend.model.QuizQuestion;
import com.learning.backend.model.UserAccount;
import com.learning.backend.repository.CourseInstructorRepository;
import com.learning.backend.repository.CourseRepository;
import com.learning.backend.repository.EnrollmentRepository;
import com.learning.backend.repository.QuizOptionRepository;
import com.learning.backend.repository.QuizQuestionRepository;
import com.learning.backend.repository.QuizRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class InstructorService {

    private final CourseInstructorRepository ownershipRepository;
    private final CourseRepository courseRepository;
    private final CourseCategoryRepository categoryRepository;
    private final CourseCategoryAssignmentRepository categoryAssignmentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final QuizRepository quizRepository;
    private final QuizQuestionRepository questionRepository;
    private final QuizOptionRepository optionRepository;

    public InstructorService(
            CourseInstructorRepository ownershipRepository,
            CourseRepository courseRepository,
            CourseCategoryRepository categoryRepository,
            CourseCategoryAssignmentRepository categoryAssignmentRepository,
            EnrollmentRepository enrollmentRepository,
            QuizRepository quizRepository,
            QuizQuestionRepository questionRepository,
            QuizOptionRepository optionRepository) {
        this.ownershipRepository = ownershipRepository;
        this.courseRepository = courseRepository;
        this.categoryRepository = categoryRepository;
        this.categoryAssignmentRepository = categoryAssignmentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.optionRepository = optionRepository;
    }

    @Transactional(readOnly = true)
    public List<InstructorCourseResponse> dashboard(UserAccount instructor) {
        return ownershipRepository.findAllByInstructorId(instructor.getUserId()).stream()
                .map(ownership -> {
                    Course course = courseRepository.findById(ownership.getCourseId())
                            .orElseThrow(() -> new ResponseStatusException(
                                    HttpStatus.NOT_FOUND, "Owned course was not found."));
                    return toResponse(course);
                })
                .toList();
    }

    @Transactional
    public InstructorCourseResponse createCourse(UserAccount instructor, ManagedCourseRequest request) {
        Course course = courseRepository.save(new Course(
                request.title().trim(),
                request.description() == null ? null : request.description().trim(),
                instructor.getName()));
        ownershipRepository.save(new CourseInstructor(course.getCourseId(), instructor.getUserId()));
        assignCategory(course, request.categoryId());
        return toResponse(course);
    }

    @Transactional
    public InstructorCourseResponse updateCourse(
            UserAccount instructor, Integer courseId, ManagedCourseRequest request) {
        Course course = ownedCourse(instructor, courseId);
        course.setTitle(request.title().trim());
        course.setDescription(request.description() == null ? null : request.description().trim());
        courseRepository.save(course);
        assignCategory(course, request.categoryId());
        return toResponse(course);
    }

    @Transactional
    public void deleteCourse(UserAccount instructor, Integer courseId) {
        Course course = ownedCourse(instructor, courseId);
        if (enrollmentRepository.countByCourseCourseId(courseId) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "A course with enrolled students cannot be deleted.");
        }
        courseRepository.delete(course);
    }

    @Transactional
    public Quiz createQuiz(UserAccount instructor, Integer courseId, QuizCreateRequest request) {
        ownedCourse(instructor, courseId);
        if (request.questions().stream().anyMatch(question ->
                question.options().stream().filter(option -> option.correct()).count() != 1)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Each question must have exactly one correct option.");
        }

        Quiz quiz = quizRepository.save(new Quiz(courseId, request.title().trim()));
        for (int questionIndex = 0; questionIndex < request.questions().size(); questionIndex++) {
            QuestionRequest questionRequest = request.questions().get(questionIndex);
            QuizQuestion question = questionRepository.save(new QuizQuestion(
                    quiz.getQuizId(), questionRequest.question().trim(), questionIndex + 1));
            for (int optionIndex = 0; optionIndex < questionRequest.options().size(); optionIndex++) {
                var option = questionRequest.options().get(optionIndex);
                optionRepository.save(new QuizOption(
                        question.getQuestionId(), option.text().trim(), optionIndex + 1, option.correct()));
            }
        }
        return quiz;
    }

    private Course ownedCourse(UserAccount instructor, Integer courseId) {
        if (ownershipRepository.findByCourseIdAndInstructorId(courseId, instructor.getUserId()).isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Course not found in your instructor account.");
        }
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found."));
    }

    private void assignCategory(Course course, Integer categoryId) {
        if (categoryId == null) {
            categoryAssignmentRepository.deleteById(course.getCourseId());
            return;
        }
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Course category not found.");
        }
        categoryAssignmentRepository.save(
                new CourseCategoryAssignment(course.getCourseId(), categoryId));
    }

    private InstructorCourseResponse toResponse(Course course) {
        return new InstructorCourseResponse(
                course.getCourseId(),
                course.getTitle(),
                course.getDescription(),
                course.getInstructor(),
                enrollmentRepository.countByCourseCourseId(course.getCourseId()));
    }
}
