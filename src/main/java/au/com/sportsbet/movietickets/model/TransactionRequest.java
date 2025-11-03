package au.com.sportsbet.movietickets.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

@Schema(description = "Request to calculate movie ticket costs for a transaction")
public record TransactionRequest(
    @Schema(description = "Unique identifier for the transaction", example = "1")
        @Positive(message = "Transaction ID must be positive")
        int transactionId,
    @Schema(description = "List of customers in the transaction")
        @NotNull(message = "Customer list cannot be null")
        @NotEmpty(message = "Customer list cannot be empty")
        List<@NotNull @Valid Customer> customers) {}
