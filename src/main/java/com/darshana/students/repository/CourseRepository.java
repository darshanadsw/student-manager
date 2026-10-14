package com.darshana.students.repository;

import com.darshana.students.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);
}
