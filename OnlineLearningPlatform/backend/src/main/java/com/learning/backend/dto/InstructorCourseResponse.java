package com.learning.backend.dto;

public record InstructorCourseResponse(
        Integer courseId,
        String title,
        String description,
        String instructor,
        long enrollmentCount) {
}
