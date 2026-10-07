package com.darshana.students;

import static org.assertj.core.api.Assertions.assertThat;

import com.darshana.students.dto.StudentRequest;
import com.darshana.students.dto.StudentResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
class StudentApiIntegrationTest {

    @LocalServerPort
    private int port;

    private RestClient client;

    @BeforeEach
    void setUp() {
        client = RestClient.builder()
                .baseUrl("http://localhost:" + port + "/api/students")
                .defaultStatusHandler(status -> true, (request, response) -> { })
                .build();
    }

    private ResponseEntity<StudentResponse> create(String name, int age, String className) {
        return client.post()
                .contentType(MediaType.APPLICATION_JSON)
                .body(new StudentRequest(name, age, className))
                .retrieve()
                .toEntity(StudentResponse.class);
    }

    @Test
    @DisplayName("Should create a student and return 201 with Location")
    void shouldCreateStudent() {
        ResponseEntity<StudentResponse> response = create("  Alice  ", 15, "10A");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        StudentResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.id()).isNotNull();
        assertThat(body.name()).isEqualTo("Alice");
        assertThat(body.age()).isEqualTo(15);
        assertThat(body.className()).isEqualTo("10A");
        assertThat(response.getHeaders().getLocation()).hasToString("/api/students/" + body.id());
    }

    @Test
    @DisplayName("Should return all students")
    void shouldReturnAllStudents() {
        create("Alice", 15, "10A");
        create("Bob", 16, "11B");

        ResponseEntity<List<StudentResponse>> response = client.get()
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() { });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).extracting(StudentResponse::name).contains("Alice", "Bob");
    }

    @Test
    @DisplayName("Should return a student by id")
    void shouldReturnStudentById() {
        StudentResponse created = create("Alice", 15, "10A").getBody();

        ResponseEntity<StudentResponse> response = client.get()
                .uri("/{id}", created.id())
                .retrieve()
                .toEntity(StudentResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(created);
    }

    @Test
    @DisplayName("Should update an existing student")
    void shouldUpdateStudent() {
        StudentResponse created = create("Alice", 15, "10A").getBody();

        ResponseEntity<StudentResponse> response = client.put()
                .uri("/{id}", created.id())
                .contentType(MediaType.APPLICATION_JSON)
                .body(new StudentRequest("Alicia", 16, "11A"))
                .retrieve()
                .toEntity(StudentResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(new StudentResponse(created.id(), "Alicia", 16, "11A"));
    }

    @Test
    @DisplayName("Should delete a student and return 204")
    void shouldDeleteStudent() {
        StudentResponse created = create("Alice", 15, "10A").getBody();

        ResponseEntity<Void> deleted = client.delete().uri("/{id}", created.id()).retrieve().toBodilessEntity();
        ResponseEntity<ProblemDetail> afterDelete = client.get()
                .uri("/{id}", created.id())
                .retrieve()
                .toEntity(ProblemDetail.class);

        assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(afterDelete.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Should return 404 ProblemDetail when student ID does not exist on get")
    void shouldReturn404OnGetWhenNotFound() {
        ResponseEntity<ProblemDetail> response = client.get().uri("/{id}", 9999).retrieve().toEntity(ProblemDetail.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getDetail()).isEqualTo("Student not found with id 9999");
    }

    @Test
    @DisplayName("Should return 404 ProblemDetail when updating a missing student")
    void shouldReturn404OnUpdateWhenNotFound() {
        ResponseEntity<ProblemDetail> response = client.put()
                .uri("/{id}", 9999)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new StudentRequest("Alice", 15, "10A"))
                .retrieve()
                .toEntity(ProblemDetail.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("Should return 404 ProblemDetail when deleting a missing student")
    void shouldReturn404OnDeleteWhenNotFound() {
        ResponseEntity<ProblemDetail> response = client.delete().uri("/{id}", 9999).retrieve().toEntity(ProblemDetail.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Should return 400 ProblemDetail when request is invalid")
    void shouldReturn400WhenInvalid() {
        ResponseEntity<ProblemDetail> response = client.post()
                .contentType(MediaType.APPLICATION_JSON)
                .body(new StudentRequest(" ", 0, ""))
                .retrieve()
                .toEntity(ProblemDetail.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("Should return 400 ProblemDetail when body is malformed")
    void shouldReturn400WhenMalformedJson() {
        ResponseEntity<ProblemDetail> response = client.post()
                .contentType(MediaType.APPLICATION_JSON)
                .body("{not json")
                .retrieve()
                .toEntity(ProblemDetail.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should return 400 ProblemDetail when id is not a number")
    void shouldReturn400WhenIdNotNumeric() {
        ResponseEntity<ProblemDetail> response = client.get().uri("/abc").retrieve().toEntity(ProblemDetail.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should serve the single page application at root")
    void shouldServeSpa() {
        String html = RestClient.create("http://localhost:" + port).get().uri("/").retrieve().body(String.class);

        assertThat(html).contains("Student Manager");
    }
}
