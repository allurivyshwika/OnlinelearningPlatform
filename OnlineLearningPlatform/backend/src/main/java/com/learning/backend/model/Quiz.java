package com.learning.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "quizzes")
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quiz_id")
    private Integer quizId;

    @Column(name = "course_id", nullable = false)
    private Integer courseId;

    @Column(nullable = false, length = 150)
    private String title;

    protected Quiz() {
    }

    public Quiz(Integer courseId, String title) {
        this.courseId = courseId;
        this.title = title;
    }

    public Integer getQuizId() {
        return quizId;
    }

    public Integer getCourseId() {
        return courseId;
    }

    public String getTitle() {
        return title;
    }
}
