package com.learning.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "quiz_options")
public class QuizOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_id")
    private Integer optionId;

    @Column(name = "question_id", nullable = false)
    private Integer questionId;

    @Column(name = "option_text", nullable = false, length = 255)
    private String optionText;

    @Column(name = "option_order", nullable = false)
    private Integer optionOrder;

    @Column(name = "is_correct", nullable = false)
    private boolean correct;

    protected QuizOption() {
    }

    public QuizOption(Integer questionId, String optionText, Integer optionOrder, boolean correct) {
        this.questionId = questionId;
        this.optionText = optionText;
        this.optionOrder = optionOrder;
        this.correct = correct;
    }

    public Integer getOptionId() {
        return optionId;
    }

    public Integer getQuestionId() {
        return questionId;
    }

    public String getOptionText() {
        return optionText;
    }

    public Integer getOptionOrder() {
        return optionOrder;
    }

    public boolean isCorrect() {
        return correct;
    }
}
