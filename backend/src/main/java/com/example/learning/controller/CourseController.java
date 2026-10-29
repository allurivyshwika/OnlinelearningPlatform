package com.example.learning.controller;

import com.example.learning.model.Course;
import com.example.learning.repository.CourseRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {
    private final CourseRepository courses;
    public CourseController(CourseRepository courses) { this.courses = courses; }

    @GetMapping
    public List<Course> all() { return courses.findAll(); }
}
