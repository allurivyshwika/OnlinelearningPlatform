package com.learning.backend.dto;

public record CourseProgressResponse(
        Integer courseId,
        String title,
        String description,
        String instructor,
        Integer progressPercent) {
}
