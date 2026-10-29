package com.learning.backend.repository;

import com.learning.backend.model.CourseCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseCategoryRepository extends JpaRepository<CourseCategory, Integer> {
    List<CourseCategory> findAllByOrderByNameAsc();

    boolean existsByNameIgnoreCase(String name);
}
