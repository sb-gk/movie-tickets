package au.com.sportsbet.movietickets.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record Customer(
    @NotBlank(message = "Customer name cannot be blank") String name,
    @Min(value = 0, message = "Age must be at least 0")
        @Max(value = 150, message = "Age must be at most 150")
        int age) {}
