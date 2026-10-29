package com.learning.backend.controller;

import com.learning.backend.dto.CategoryResponse;
import com.learning.backend.repository.CourseCategoryRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CategoryController {

    private final CourseCategoryRepository categoryRepository;

    public CategoryController(CourseCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @GetMapping("/api/categories")
    public List<CategoryResponse> categories() {
        return categoryRepository.findAllByOrderByNameAsc().stream()
                .map(category -> new CategoryResponse(category.getCategoryId(), category.getName()))
                .toList();
    }
}
