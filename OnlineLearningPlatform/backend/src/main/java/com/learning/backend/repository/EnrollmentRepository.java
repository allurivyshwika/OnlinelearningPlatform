package com.learning.backend.repository;

import com.learning.backend.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Integer> {
    boolean existsByUserUserIdAndCourseCourseId(Integer userId, Integer courseId);

    List<Enrollment> findByUserUserIdOrderByEnrolledAtDesc(Integer userId);

    long countByCourseCourseId(Integer courseId);
}
