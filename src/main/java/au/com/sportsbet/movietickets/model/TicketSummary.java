package au.com.sportsbet.movietickets.model;

import java.math.BigDecimal;

public record TicketSummary(String ticketType, int quantity, BigDecimal totalCost) {}
