package au.com.sportsbet.movietickets.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record TransactionRequest(
    @Positive(message = "Transaction ID must be positive")
    int transactionId,
    
    @NotNull(message = "Customer list cannot be null")
    @NotEmpty(message = "Customer list cannot be empty")
    List<@Valid Customer> customers
) {
}