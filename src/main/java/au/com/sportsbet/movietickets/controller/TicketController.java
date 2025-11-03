package au.com.sportsbet.movietickets.controller;

import au.com.sportsbet.movietickets.model.TransactionRequest;
import au.com.sportsbet.movietickets.model.TransactionResponse;
import au.com.sportsbet.movietickets.service.TransactionProcessor;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

  private final TransactionProcessor transactionProcessor;

  public TicketController(TransactionProcessor transactionProcessor) {
    this.transactionProcessor = transactionProcessor;
  }

  @PostMapping("/calculate")
  public ResponseEntity<TransactionResponse> calculateTicketCost(
      @Valid @RequestBody TransactionRequest request) {
    TransactionResponse response = transactionProcessor.processTransaction(request);
    return ResponseEntity.ok(response);
  }
}
