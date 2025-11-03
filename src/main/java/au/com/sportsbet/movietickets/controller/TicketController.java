package au.com.sportsbet.movietickets.controller;

import au.com.sportsbet.movietickets.model.TransactionRequest;
import au.com.sportsbet.movietickets.model.TransactionResponse;
import au.com.sportsbet.movietickets.service.TransactionProcessor;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private static final Logger log = LoggerFactory.getLogger(TicketController.class);
    private final TransactionProcessor transactionProcessor;

    public TicketController(TransactionProcessor transactionProcessor) {
        this.transactionProcessor = transactionProcessor;
    }

    @PostMapping("/calculate")
    public ResponseEntity<TransactionResponse> calculateTicketCost(
            @Valid @RequestBody TransactionRequest request) {
        log.info("Received ticket calculation request for transaction ID: {}", request.transactionId());
        log.debug("Processing request with {} customers", request.customers().size());

        TransactionResponse response = transactionProcessor.processTransaction(request);

        log.info("Successfully calculated ticket cost for transaction ID: {} - Total: ${}",
                response.transactionId(), response.totalCost());
        log.debug("Response contains {} ticket types", response.tickets().size());

        return ResponseEntity.ok(response);
    }
}
