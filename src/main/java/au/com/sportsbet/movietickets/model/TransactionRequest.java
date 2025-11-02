package au.com.sportsbet.movietickets.model;

import java.util.List;

public record TransactionRequest(
    int transactionId,
    List<Customer> customers
) {
}