package com.learning.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "course_instructors")
public class CourseInstructor {

    @Id
    @Column(name = "course_id")
    private Integer courseId;

    @Column(name = "instructor_id", nullable = false)
    private Integer instructorId;

    protected CourseInstructor() {
    }

    public CourseInstructor(Integer courseId, Integer instructorId) {
        this.courseId = courseId;
        this.instructorId = instructorId;
    }

    public Integer getCourseId() {
        return courseId;
    }

    public Integer getInstructorId() {
        return instructorId;
    }
}
