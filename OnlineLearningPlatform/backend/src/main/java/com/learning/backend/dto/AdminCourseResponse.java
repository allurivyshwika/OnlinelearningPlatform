package com.learning.backend.dto;

public record AdminCourseResponse(
        Integer courseId,
        String title,
        String instructor,
        Integer instructorId,
        long enrollmentCount) {
}
