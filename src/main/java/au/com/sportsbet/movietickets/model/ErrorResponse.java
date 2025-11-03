package au.com.sportsbet.movietickets.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Error response for API failures")
public record ErrorResponse(
    @Schema(description = "Timestamp when the error occurred")
    LocalDateTime timestamp,
    @Schema(description = "HTTP status code", example = "400")
    int status,
    @Schema(description = "Error type", example = "Validation Failed")
    String error,
    @Schema(description = "Error message", example = "Invalid request parameters")
    String message,
    @Schema(description = "List of detailed error messages")
    List<String> details) {
  public ErrorResponse(int status, String error, String message) {
    this(LocalDateTime.now(), status, error, message, List.of());
  }

  public ErrorResponse(int status, String error, String message, List<String> details) {
    this(LocalDateTime.now(), status, error, message, details);
  }
}
