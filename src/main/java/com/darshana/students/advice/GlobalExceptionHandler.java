package com.darshana.students.advice;

import com.darshana.students.exceptions.CourseNotFoundException;
import com.darshana.students.exceptions.DuplicateCourseCodeException;
import com.darshana.students.exceptions.StudentNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Extends ResponseEntityExceptionHandler so framework errors (validation, malformed JSON,
 * type mismatch) are also rendered as RFC 7807 ProblemDetail.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(StudentNotFoundException.class)
    public ProblemDetail handleNotFound(StudentNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Student not found");
        return problem;
    }

    @ExceptionHandler(CourseNotFoundException.class)
    public ProblemDetail handleCourseNotFound(CourseNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Course not found");
        return problem;
    }

    @ExceptionHandler(DuplicateCourseCodeException.class)
    public ProblemDetail handleDuplicateCourseCode(DuplicateCourseCodeException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Duplicate course code");
        return problem;
    }
}
