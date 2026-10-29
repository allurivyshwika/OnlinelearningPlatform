package com.learning.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "course_category_assignments")
public class CourseCategoryAssignment {

    @Id
    @Column(name = "course_id")
    private Integer courseId;

    @Column(name = "category_id", nullable = false)
    private Integer categoryId;

    protected CourseCategoryAssignment() {
    }

    public CourseCategoryAssignment(Integer courseId, Integer categoryId) {
        this.courseId = courseId;
        this.categoryId = categoryId;
    }
}
