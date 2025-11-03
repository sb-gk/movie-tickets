package au.com.sportsbet.movietickets.model;

import java.math.BigDecimal;
import java.util.List;

public record TransactionResponse(
    int transactionId, List<TicketSummary> tickets, BigDecimal totalCost) {}
