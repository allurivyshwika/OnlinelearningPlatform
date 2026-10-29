package com.learning.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "quiz_questions")
public class QuizQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Integer questionId;

    @Column(name = "quiz_id", nullable = false)
    private Integer quizId;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "question_order", nullable = false)
    private Integer questionOrder;

    protected QuizQuestion() {
    }

    public QuizQuestion(Integer quizId, String questionText, Integer questionOrder) {
        this.quizId = quizId;
        this.questionText = questionText;
        this.questionOrder = questionOrder;
    }

    public Integer getQuestionId() {
        return questionId;
    }

    public Integer getQuizId() {
        return quizId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public Integer getQuestionOrder() {
        return questionOrder;
    }
}
