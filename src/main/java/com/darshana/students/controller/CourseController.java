package com.darshana.students.controller;

import com.darshana.students.dto.CourseRequest;
import com.darshana.students.dto.CourseResponse;
import com.darshana.students.service.CourseService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService service;

    public CourseController(CourseService service) {
        this.service = service;
    }

    @GetMapping
    public List<CourseResponse> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public CourseResponse getById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<CourseResponse> create(@Valid @RequestBody CourseRequest request) {
        CourseResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/courses/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public CourseResponse update(@PathVariable Long id, @Valid @RequestBody CourseRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/students/{studentId}")
    public ResponseEntity<Void> enrol(@PathVariable Long id, @PathVariable Long studentId) {
        service.enrol(id, studentId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/students/{studentId}")
    public ResponseEntity<Void> unenrol(@PathVariable Long id, @PathVariable Long studentId) {
        service.unenrol(id, studentId);
        return ResponseEntity.noContent().build();
    }
}
