package com.learning.backend.repository;

import com.learning.backend.model.LearningProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LearningProgressRepository extends JpaRepository<LearningProgress, Integer> {
    Optional<LearningProgress> findFirstByUserUserIdAndCourseCourseId(Integer userId, Integer courseId);
}
