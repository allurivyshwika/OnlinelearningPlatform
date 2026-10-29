package com.learning.backend.dto;

public record AdminStatisticsResponse(long students, long instructors, long admins, long courses) {
}
