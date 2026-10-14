package com.darshana.students.mapper;

import com.darshana.students.dto.CourseRequest;
import com.darshana.students.dto.CourseResponse;
import com.darshana.students.entity.Course;
import org.springframework.stereotype.Component;

@Component
public class CourseMapper {

    public Course toEntity(CourseRequest request) {
        return new Course(
                request.name().trim(),
                request.code().trim(),
                request.description() != null ? request.description().trim() : null,
                request.credits()
        );
    }

    public void updateEntity(Course course, CourseRequest request) {
        course.setName(request.name().trim());
        course.setCode(request.code().trim());
        course.setDescription(request.description() != null ? request.description().trim() : null);
        course.setCredits(request.credits());
    }

    public CourseResponse toResponse(Course course) {
        return new CourseResponse(
                course.getId(),
                course.getName(),
                course.getCode(),
                course.getDescription(),
                course.getCredits()
        );
    }
}
