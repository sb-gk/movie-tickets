package au.com.sportsbet.movietickets.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(
    description = "Error response for API failures",
    example =
        "{\"timestamp\":\"2025-01-01T12:00:00\",\"status\":400,\"error\":\"Validation Failed\",\"message\":\"Invalid request parameters\",\"details\":[\"customers[0].name: Customer name cannot be blank\",\"transactionId: Transaction ID must be positive\"]}")
public record ErrorResponse(
    @Schema(
            description = "Timestamp when the error occurred",
            format = "date-time",
            example = "2025-01-01T12:00:00")
        LocalDateTime timestamp,
    @Schema(description = "HTTP status code", example = "400") int status,
    @Schema(description = "Error type", example = "Validation Failed") String error,
    @Schema(description = "Error message", example = "Invalid request parameters") String message,
    @Schema(
            description = "List of detailed error messages",
            example =
                "[\"customers[0].name: Customer name cannot be blank\",\"transactionId: Transaction ID must be positive\"]")
        List<String> details) {
  public ErrorResponse(int status, String error, String message) {
    this(LocalDateTime.now(), status, error, message, List.of());
  }

  public ErrorResponse(int status, String error, String message, List<String> details) {
    this(LocalDateTime.now(), status, error, message, details);
  }
}
