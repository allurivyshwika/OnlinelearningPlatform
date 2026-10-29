package com.learning.backend.repository;

import com.learning.backend.model.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Integer> {
    List<QuizAttempt> findAllByUserIdOrderByAttemptedAtDesc(Integer userId);
}
