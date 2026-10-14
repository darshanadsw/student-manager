package com.darshana.students.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CourseRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 20) String code,
        @Size(max = 500) String description,
        @NotNull @Min(1) @Max(10) Integer credits
) {
}
