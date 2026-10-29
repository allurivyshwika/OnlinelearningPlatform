package com.learning.backend.repository;

import com.learning.backend.model.CourseCategoryAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseCategoryAssignmentRepository
        extends JpaRepository<CourseCategoryAssignment, Integer> {
}
