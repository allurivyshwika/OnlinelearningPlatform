package com.learning.backend.repository;

import com.learning.backend.model.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Integer> {
    List<QuizQuestion> findAllByQuizIdOrderByQuestionOrder(Integer quizId);
}
