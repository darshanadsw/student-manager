package com.darshana.students.mapper;

import com.darshana.students.dto.StudentRequest;
import com.darshana.students.dto.StudentResponse;
import com.darshana.students.entity.Student;
import org.springframework.stereotype.Component;

@Component
public class StudentMapper {

    public Student toEntity(StudentRequest request) {
        return new Student(request.name().trim(), request.age(), request.className().trim());
    }

    public void updateEntity(Student student, StudentRequest request) {
        student.setName(request.name().trim());
        student.setAge(request.age());
        student.setClassName(request.className().trim());
    }

    public StudentResponse toResponse(Student student) {
        return new StudentResponse(student.getId(), student.getName(), student.getAge(), student.getClassName());
    }
}
