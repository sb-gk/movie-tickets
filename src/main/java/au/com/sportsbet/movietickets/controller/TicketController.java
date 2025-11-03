package au.com.sportsbet.movietickets.controller;

import au.com.sportsbet.movietickets.model.TransactionRequest;
import au.com.sportsbet.movietickets.model.TransactionResponse;
import au.com.sportsbet.movietickets.service.TransactionProcessor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
@Tag(name = "Movie Tickets API", description = "API for calculating movie ticket costs")
public class TicketController {

  private static final Logger log = LoggerFactory.getLogger(TicketController.class);
  private final TransactionProcessor transactionProcessor;

  public TicketController(TransactionProcessor transactionProcessor) {
    this.transactionProcessor = transactionProcessor;
  }

  /**
   * Calculates the total ticket cost for a movie transaction.
   *
   * @param request the transaction request containing transaction ID and customer list
   * @return ResponseEntity containing the transaction response with ticket summaries and total cost
   */
  @Operation(
      summary = "Calculate movie ticket costs",
      description =
          "Calculates the total cost of movie tickets for a transaction, including any applicable discounts for children groups.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully calculated ticket costs",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TransactionResponse.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request parameters",
            content =
                @Content(
                    mediaType = "application/json",
                    schema =
                        @Schema(
                            implementation =
                                au.com.sportsbet.movietickets.model.ErrorResponse.class)))
      })
  @PostMapping("/calculate")
  public ResponseEntity<TransactionResponse> calculateTicketCost(
      @Valid @RequestBody TransactionRequest request) {
    log.info("Received ticket calculation request for transaction ID: {}", request.transactionId());
    log.debug("Processing request with {} customers", request.customers().size());

    TransactionResponse response = transactionProcessor.processTransaction(request);

    log.info(
        "Successfully calculated ticket cost for transaction ID: {} - Total: ${}",
        response.transactionId(),
        response.totalCost());
    log.debug("Response contains {} ticket types", response.tickets().size());

    return ResponseEntity.ok(response);
  }
}
