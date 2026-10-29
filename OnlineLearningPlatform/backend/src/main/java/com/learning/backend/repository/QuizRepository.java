package com.learning.backend.repository;

import com.learning.backend.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Integer> {
    List<Quiz> findAllByCourseIdOrderByQuizId(Integer courseId);
}
