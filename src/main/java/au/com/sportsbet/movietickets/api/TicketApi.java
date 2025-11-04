package au.com.sportsbet.movietickets.api;

import au.com.sportsbet.movietickets.model.TransactionRequest;
import au.com.sportsbet.movietickets.model.TransactionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

@Tag(name = "Movie Tickets API", description = "API for calculating movie ticket costs")
public interface TicketApi {

  @Operation(
      summary = "Calculate movie ticket costs",
      description =
          "Calculates the total cost of movie tickets for a transaction, including any applicable discounts for children groups.",
      requestBody =
          @RequestBody(
              required = true,
              description = "Transaction input containing transactionId and customers",
              content =
                  @Content(
                      mediaType = "application/json",
                      schema = @Schema(implementation = TransactionRequest.class),
                      examples = {
                        @ExampleObject(
                            name = "SampleRequest",
                            summary = "Example request mirroring PDF sample",
                            value =
                                "{\n  \"transactionId\": 1,\n  \"customers\": [\n    { \"name\": \"John Smith\", \"age\": 70 },\n    { \"name\": \"Jane Doe\", \"age\": 5 },\n    { \"name\": \"Bob Doe\", \"age\": 6 }\n  ]\n}")
                      })))
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully calculated ticket costs",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TransactionResponse.class),
                    examples = {
                      @ExampleObject(
                          name = "SampleResponse",
                          summary = "Sample successful calculation",
                          value =
                              "{\n  \"transactionId\": 1,\n  \"tickets\": [\n    { \"ticketType\": \"Children\", \"quantity\": 2, \"totalCost\": 10.00 },\n    { \"ticketType\": \"Senior\", \"quantity\": 1, \"totalCost\": 17.50 }\n  ],\n  \"totalCost\": 27.50\n}")
                    })),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request parameters",
            content =
                @Content(
                    mediaType = "application/json",
                    schema =
                        @Schema(
                            implementation =
                                au.com.sportsbet.movietickets.model.ErrorResponse.class),
                    examples = {
                      @ExampleObject(
                          name = "ValidationError",
                          summary = "Example validation error payload",
                          value =
                              "{\n  \"timestamp\": \"2025-01-01T12:00:00\",\n  \"status\": 400,\n  \"error\": \"Validation Failed\",\n  \"message\": \"Invalid request parameters\",\n  \"details\": [\n    \"customers[0].name: Customer name cannot be blank\",\n    \"transactionId: Transaction ID must be positive\"\n  ]\n}")
                    }))
      })
  ResponseEntity<TransactionResponse> calculateTicketCost(
      @Valid @org.springframework.web.bind.annotation.RequestBody TransactionRequest request);
}
