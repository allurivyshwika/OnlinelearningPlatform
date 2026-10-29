package com.learning.backend.repository;

import com.learning.backend.model.QuizOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizOptionRepository extends JpaRepository<QuizOption, Integer> {
    List<QuizOption> findAllByQuestionIdOrderByOptionOrder(Integer questionId);
}
