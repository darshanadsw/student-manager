package com.darshana.students.service;

import com.darshana.students.dto.CourseRequest;
import com.darshana.students.dto.CourseResponse;
import com.darshana.students.entity.Course;
import com.darshana.students.entity.Student;
import com.darshana.students.exceptions.CourseNotFoundException;
import com.darshana.students.exceptions.DuplicateCourseCodeException;
import com.darshana.students.exceptions.StudentNotFoundException;
import com.darshana.students.mapper.CourseMapper;
import com.darshana.students.repository.CourseRepository;
import com.darshana.students.repository.StudentRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CourseService {

    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;
    private final CourseMapper mapper;

    public CourseService(CourseRepository courseRepository, StudentRepository studentRepository, CourseMapper mapper) {
        this.courseRepository = courseRepository;
        this.studentRepository = studentRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> findAll() {
        return courseRepository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CourseResponse findById(Long id) {
        return mapper.toResponse(getCourseOrThrow(id));
    }

    public CourseResponse create(CourseRequest request) {
        if (courseRepository.existsByCode(request.code().trim())) {
            throw new DuplicateCourseCodeException(request.code().trim());
        }
        return mapper.toResponse(courseRepository.save(mapper.toEntity(request)));
    }

    public CourseResponse update(Long id, CourseRequest request) {
        Course course = getCourseOrThrow(id);
        if (courseRepository.existsByCodeAndIdNot(request.code().trim(), id)) {
            throw new DuplicateCourseCodeException(request.code().trim());
        }
        mapper.updateEntity(course, request);
        return mapper.toResponse(courseRepository.save(course));
    }

    public void delete(Long id) {
        courseRepository.delete(getCourseOrThrow(id));
    }

    public void enrol(Long courseId, Long studentId) {
        Course course = getCourseOrThrow(courseId);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException(studentId));
        course.addStudent(student);
    }

    public void unenrol(Long courseId, Long studentId) {
        Course course = getCourseOrThrow(courseId);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException(studentId));
        course.removeStudent(student);
    }

    private Course getCourseOrThrow(Long id) {
        return courseRepository.findById(id).orElseThrow(() -> new CourseNotFoundException(id));
    }
}
