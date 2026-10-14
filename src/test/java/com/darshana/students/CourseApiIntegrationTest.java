package com.darshana.students;

import static org.assertj.core.api.Assertions.assertThat;

import com.darshana.students.dto.CourseRequest;
import com.darshana.students.dto.CourseResponse;
import com.darshana.students.dto.StudentRequest;
import com.darshana.students.dto.StudentResponse;
import com.darshana.students.repository.CourseRepository;
import com.darshana.students.repository.StudentRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CourseApiIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private StudentRepository studentRepository;

    private RestClient courseClient;
    private RestClient studentClient;

    @BeforeEach
    void setUp() {
        courseRepository.deleteAll();
        studentRepository.deleteAll();
        courseClient = RestClient.builder()
                .baseUrl("http://localhost:" + port + "/api/courses")
                .defaultStatusHandler(status -> true, (request, response) -> { })
                .build();
        studentClient = RestClient.builder()
                .baseUrl("http://localhost:" + port + "/api/students")
                .defaultStatusHandler(status -> true, (request, response) -> { })
                .build();
    }

    private ResponseEntity<CourseResponse> createCourse(String name, String code, String description, int credits) {
        return courseClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CourseRequest(name, code, description, credits))
                .retrieve()
                .toEntity(CourseResponse.class);
    }

    private ResponseEntity<StudentResponse> createStudent(String name, int age, String className) {
        return studentClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .body(new StudentRequest(name, age, className))
                .retrieve()
                .toEntity(StudentResponse.class);
    }

    @Test
    @DisplayName("Should create a course and return 201 with Location")
    void shouldCreateCourse() {
        ResponseEntity<CourseResponse> response = createCourse("Mathematics", "MATH101", "Introduction to Maths", 3);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        CourseResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.id()).isNotNull();
        assertThat(body.name()).isEqualTo("Mathematics");
        assertThat(body.code()).isEqualTo("MATH101");
        assertThat(body.description()).isEqualTo("Introduction to Maths");
        assertThat(body.credits()).isEqualTo(3);
        assertThat(response.getHeaders().getLocation()).hasToString("/api/courses/" + body.id());
    }

    @Test
    @DisplayName("Should return all courses")
    void shouldReturnAllCourses() {
        createCourse("Mathematics", "MATH101", null, 3);
        createCourse("Physics", "PHYS101", null, 4);

        ResponseEntity<List<CourseResponse>> response = courseClient.get()
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() { });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).extracting(CourseResponse::code).contains("MATH101", "PHYS101");
    }

    @Test
    @DisplayName("Should return a course by id")
    void shouldReturnCourseById() {
        CourseResponse created = createCourse("Mathematics", "MATH101", "Intro", 3).getBody();

        ResponseEntity<CourseResponse> response = courseClient.get()
                .uri("/{id}", created.id())
                .retrieve()
                .toEntity(CourseResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(created);
    }

    @Test
    @DisplayName("Should update an existing course")
    void shouldUpdateCourse() {
        CourseResponse created = createCourse("Mathematics", "MATH101", "Intro", 3).getBody();

        ResponseEntity<CourseResponse> response = courseClient.put()
                .uri("/{id}", created.id())
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CourseRequest("Advanced Mathematics", "MATH201", "Advanced", 5))
                .retrieve()
                .toEntity(CourseResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        CourseResponse body = response.getBody();
        assertThat(body.name()).isEqualTo("Advanced Mathematics");
        assertThat(body.code()).isEqualTo("MATH201");
        assertThat(body.credits()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should delete a course and return 204")
    void shouldDeleteCourse() {
        CourseResponse created = createCourse("Mathematics", "MATH101", null, 3).getBody();

        ResponseEntity<Void> deleted = courseClient.delete().uri("/{id}", created.id()).retrieve().toBodilessEntity();
        ResponseEntity<ProblemDetail> afterDelete = courseClient.get()
                .uri("/{id}", created.id())
                .retrieve()
                .toEntity(ProblemDetail.class);

        assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(afterDelete.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Should return 409 when creating course with duplicate code")
    void shouldReturn409OnDuplicateCodeCreate() {
        createCourse("Mathematics", "MATH101", null, 3);

        ResponseEntity<ProblemDetail> response = courseClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CourseRequest("Other Math", "MATH101", null, 2))
                .retrieve()
                .toEntity(ProblemDetail.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(409);
    }

    @Test
    @DisplayName("Should return 409 when updating course to a duplicate code")
    void shouldReturn409OnDuplicateCodeUpdate() {
        createCourse("Mathematics", "MATH101", null, 3);
        CourseResponse second = createCourse("Physics", "PHYS101", null, 4).getBody();

        ResponseEntity<ProblemDetail> response = courseClient.put()
                .uri("/{id}", second.id())
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CourseRequest("Physics", "MATH101", null, 4))
                .retrieve()
                .toEntity(ProblemDetail.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(409);
    }

    @Test
    @DisplayName("Should return 404 ProblemDetail when course id does not exist")
    void shouldReturn404WhenNotFound() {
        ResponseEntity<ProblemDetail> response = courseClient.get().uri("/{id}", 9999).retrieve().toEntity(ProblemDetail.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getDetail()).isEqualTo("Course not found with id 9999");
    }

    @Test
    @DisplayName("Should return 400 ProblemDetail when request is invalid")
    void shouldReturn400WhenInvalid() {
        ResponseEntity<ProblemDetail> response = courseClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CourseRequest(" ", " ", null, 0))
                .retrieve()
                .toEntity(ProblemDetail.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("Should enrol student in course and return 204")
    void shouldEnrolStudent() {
        CourseResponse course = createCourse("Mathematics", "MATH101", null, 3).getBody();
        StudentResponse student = createStudent("Alice", 15, "10A").getBody();

        ResponseEntity<Void> response = courseClient.post()
                .uri("/{id}/students/{studentId}", course.id(), student.id())
                .retrieve()
                .toBodilessEntity();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("Should unenrol student from course and return 204")
    void shouldUnenrolStudent() {
        CourseResponse course = createCourse("Mathematics", "MATH101", null, 3).getBody();
        StudentResponse student = createStudent("Alice", 15, "10A").getBody();

        courseClient.post().uri("/{id}/students/{studentId}", course.id(), student.id()).retrieve().toBodilessEntity();

        ResponseEntity<Void> response = courseClient.delete()
                .uri("/{id}/students/{studentId}", course.id(), student.id())
                .retrieve()
                .toBodilessEntity();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("Should return 404 when enrolling to non-existent course")
    void shouldReturn404WhenEnrollingToMissingCourse() {
        StudentResponse student = createStudent("Alice", 15, "10A").getBody();

        ResponseEntity<ProblemDetail> response = courseClient.post()
                .uri("/{id}/students/{studentId}", 9999, student.id())
                .retrieve()
                .toEntity(ProblemDetail.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Should return 404 when enrolling non-existent student")
    void shouldReturn404WhenEnrollingMissingStudent() {
        CourseResponse course = createCourse("Mathematics", "MATH101", null, 3).getBody();

        ResponseEntity<ProblemDetail> response = courseClient.post()
                .uri("/{id}/students/{studentId}", course.id(), 9999)
                .retrieve()
                .toEntity(ProblemDetail.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
