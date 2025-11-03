package au.com.sportsbet.movietickets.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Customer information for ticket calculation")
public record Customer(
    @Schema(description = "Customer's full name", example = "John Smith")
        @NotBlank(message = "Customer name cannot be blank")
        String name,
    @Schema(description = "Customer's age in years", example = "30", minimum = "0", maximum = "150")
        @Min(value = 0, message = "Age must be at least 0")
        @Max(value = 150, message = "Age must be at most 150")
        int age) {}
