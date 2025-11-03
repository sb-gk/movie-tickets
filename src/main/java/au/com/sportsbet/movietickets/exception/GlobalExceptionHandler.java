package au.com.sportsbet.movietickets.exception;

import au.com.sportsbet.movietickets.model.ErrorResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ApiResponse(
      responseCode = "400",
      description = "Validation failure",
      content = @Content(
          mediaType = "application/json",
          schema = @Schema(implementation = ErrorResponse.class),
          examples = {
            @ExampleObject(
                name = "ValidationError",
                summary = "Example validation error payload",
                value = "{\n  \"timestamp\": \"2025-01-01T12:00:00\",\n  \"status\": 400,\n  \"error\": \"Validation Failed\",\n  \"message\": \"Invalid request parameters\",\n  \"details\": [\n    \"customers[0].name: Customer name cannot be blank\",\n    \"transactionId: Transaction ID must be positive\"\n  ]\n}")
          }
      )
  )
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(
      MethodArgumentNotValidException ex) {
    List<String> errors =
        ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.toList());

    log.warn("Validation failed with {} errors: {}", errors.size(), errors);

    ErrorResponse errorResponse =
        new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Validation Failed",
            "Invalid request parameters",
            errors);

    return ResponseEntity.badRequest().body(errorResponse);
  }

  @ApiResponse(
      responseCode = "400",
      description = "Illegal argument in request",
      content = @Content(
          mediaType = "application/json",
          schema = @Schema(implementation = ErrorResponse.class),
          examples = {
            @ExampleObject(
                name = "BadRequest",
                summary = "Generic bad request example",
                value = "{\n  \"timestamp\": \"2025-01-01T12:00:00\",\n  \"status\": 400,\n  \"error\": \"Bad Request\",\n  \"message\": \"Adult price must be positive\",\n  \"details\": []\n}")
          }
      )
  )
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
    log.warn("Illegal argument exception: {}", ex.getMessage());

    ErrorResponse errorResponse =
        new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Bad Request", ex.getMessage());

    return ResponseEntity.badRequest().body(errorResponse);
  }

  @ApiResponse(
      responseCode = "500",
      description = "Illegal state or server error",
      content = @Content(
          mediaType = "application/json",
          schema = @Schema(implementation = ErrorResponse.class),
          examples = {
            @ExampleObject(
                name = "IllegalState",
                summary = "Illegal state example",
                value = "{\n  \"timestamp\": \"2025-01-01T12:00:00\",\n  \"status\": 500,\n  \"error\": \"Internal Server Error\",\n  \"message\": \"Age configuration leaves a gap\",\n  \"details\": []\n}")
          }
      )
  )
  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException ex) {
    log.error("Illegal state exception: {}", ex.getMessage(), ex);

    ErrorResponse errorResponse =
        new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error", ex.getMessage());

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
  }

  @ApiResponse(
      responseCode = "500",
      description = "Unexpected server error",
      content = @Content(
          mediaType = "application/json",
          schema = @Schema(implementation = ErrorResponse.class),
          examples = {
            @ExampleObject(
                name = "UnexpectedError",
                summary = "Generic 500 error example",
                value = "{\n  \"timestamp\": \"2025-01-01T12:00:00\",\n  \"status\": 500,\n  \"error\": \"Internal Server Error\",\n  \"message\": \"An unexpected error occurred: NullPointerException\",\n  \"details\": []\n}")
          }
      )
  )
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
    log.error("Unexpected error occurred: {}", ex.getMessage(), ex);

    ErrorResponse errorResponse =
        new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            "An unexpected error occurred: " + ex.getMessage());

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
  }
}
