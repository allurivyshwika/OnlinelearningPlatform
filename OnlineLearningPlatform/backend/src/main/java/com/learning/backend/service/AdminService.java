package com.learning.backend.service;

import com.learning.backend.dto.AdminStatisticsResponse;
import com.learning.backend.dto.AdminCourseResponse;
import com.learning.backend.dto.CategoryRequest;
import com.learning.backend.dto.CategoryResponse;
import com.learning.backend.dto.InstructorAssignmentRequest;
import com.learning.backend.dto.ManagedCourseRequest;
import com.learning.backend.dto.ManagedUserResponse;
import com.learning.backend.dto.QuestionRequest;
import com.learning.backend.dto.QuizCreateRequest;
import com.learning.backend.dto.RoleUpdateRequest;
import com.learning.backend.model.Course;
import com.learning.backend.model.CourseCategory;
import com.learning.backend.model.CourseCategoryAssignment;
import com.learning.backend.model.Quiz;
import com.learning.backend.model.QuizOption;
import com.learning.backend.model.QuizQuestion;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
@Service
public class AdminService {

    private final UserAccountRepository userRepository;
    private final CourseRepository courseRepository;
    private final CourseCategoryRepository categoryRepository;
    private final CourseCategoryAssignmentRepository categoryAssignmentRepository;
    private final CourseInstructorRepository courseInstructorRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final QuizRepository quizRepository;
    private final QuizQuestionRepository questionRepository;
    private final QuizOptionRepository optionRepository;

    public AdminService(
            UserAccountRepository userRepository,
            CourseRepository courseRepository,
            CourseCategoryRepository categoryRepository,
            CourseCategoryAssignmentRepository categoryAssignmentRepository,
            CourseInstructorRepository courseInstructorRepository,
            EnrollmentRepository enrollmentRepository,
            QuizRepository quizRepository,
            QuizQuestionRepository questionRepository,
            QuizOptionRepository optionRepository) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.categoryRepository = categoryRepository;
        this.categoryAssignmentRepository = categoryAssignmentRepository;
        this.courseInstructorRepository = courseInstructorRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.optionRepository = optionRepository;
    }

    @Transactional(readOnly = true)
    public AdminStatisticsResponse statistics() {
        return new AdminStatisticsResponse(
                userRepository.countByRole("STUDENT"),
                userRepository.countByRole("INSTRUCTOR"),
                userRepository.countByRole("ADMIN"),
                courseRepository.count());
    }

    @Transactional(readOnly = true)
    public List<ManagedUserResponse> users() {
        return userRepository.findAllByOrderByUserIdDesc().stream()
                .map(user -> new ManagedUserResponse(
                        user.getUserId(), user.getName(), user.getEmail(), user.getRole()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminCourseResponse> courses() {
        return courseRepository.findAll().stream()
                .map(course -> {
                    Integer instructorId = courseInstructorRepository.findById(course.getCourseId())
                            .map(ownership -> ownership.getInstructorId())
                            .orElse(null);
                    return new AdminCourseResponse(
                            course.getCourseId(),
                            course.getTitle(),
                            course.getInstructor(),
                            instructorId,
                            enrollmentRepository.countByCourseCourseId(course.getCourseId()));
                })
                .toList();
    }

    @Transactional
    public AdminCourseResponse createCourse(ManagedCourseRequest request) {
        if (request.categoryId() != null && !categoryRepository.existsById(request.categoryId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Course category not found.");
        }
        Course course = courseRepository.save(new Course(
                request.title().trim(),
                request.description() == null ? null : request.description().trim(),
                null));
        if (request.categoryId() != null) {
            categoryAssignmentRepository.save(
                    new CourseCategoryAssignment(course.getCourseId(), request.categoryId()));
        }
        return new AdminCourseResponse(
                course.getCourseId(),
                course.getTitle(),
                course.getInstructor(),
                null,
                0);
    }

    @Transactional
    public Quiz createQuiz(Integer courseId, QuizCreateRequest request) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found.");
        }
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

    @Transactional
    public AdminCourseResponse assignInstructor(
            Integer courseId, InstructorAssignmentRequest request) {
        var course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found."));
        UserAccount instructor = userRepository.findById(request.instructorId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Instructor not found."));
        if (!"INSTRUCTOR".equals(instructor.getRole())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The selected user is not an instructor.");
        }
        courseInstructorRepository.save(
                new com.learning.backend.model.CourseInstructor(courseId, instructor.getUserId()));
        course.setInstructor(instructor.getName());
        courseRepository.save(course);
        return new AdminCourseResponse(
                course.getCourseId(),
                course.getTitle(),
                course.getInstructor(),
                instructor.getUserId(),
                enrollmentRepository.countByCourseCourseId(courseId));
    }

    @Transactional
    public void deleteCourse(Integer courseId) {
        var course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found."));
        if (enrollmentRepository.countByCourseCourseId(courseId) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "A course with enrolled students cannot be deleted.");
        }
        courseRepository.delete(course);
    }

    @Transactional
    public ManagedUserResponse updateRole(Integer userId, RoleUpdateRequest request, UserAccount actor) {
        UserAccount user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));
        if (actor.getUserId().equals(userId) && !"ADMIN".equals(request.role())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "You cannot remove your own administrator role.");
        }
        user.setRole(request.role());
        return new ManagedUserResponse(
                user.getUserId(), user.getName(), user.getEmail(), user.getRole());
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> categories() {
        return categoryRepository.findAllByOrderByNameAsc().stream()
                .map(category -> new CategoryResponse(category.getCategoryId(), category.getName()))
                .toList();
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        String name = request.name().trim();
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "That category already exists.");
        }
        CourseCategory category = categoryRepository.save(new CourseCategory(name));
        return new CategoryResponse(category.getCategoryId(), category.getName());
    }

    @Transactional
    public void deleteCategory(Integer categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found.");
        }
        categoryRepository.deleteById(categoryId);
    }
}
