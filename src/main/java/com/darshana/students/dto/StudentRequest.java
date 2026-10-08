package com.darshana.students.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StudentRequest(
        @NotBlank @Size(max = 100) String name,
        @NotNull @Min(1) @Max(120) Integer age,
        @NotBlank @Size(max = 50) String className) {
}
