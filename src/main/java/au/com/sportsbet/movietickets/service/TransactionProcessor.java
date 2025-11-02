package au.com.sportsbet.movietickets.service;

import au.com.sportsbet.movietickets.config.AgeConfiguration;
import au.com.sportsbet.movietickets.model.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TransactionProcessor {

    private final TicketPriceCalculator priceCalculator;
    private final AgeConfiguration ageConfig;

    public TransactionProcessor(TicketPriceCalculator priceCalculator, AgeConfiguration ageConfig) {
        this.priceCalculator = priceCalculator;
        this.ageConfig = ageConfig;
    }

    public TransactionResponse processTransaction(TransactionRequest request) {
        // Group customers by ticket type
        Map<TicketType, List<Customer>> customersByType = request.customers().stream()
            .collect(Collectors.groupingBy(customer -> TicketType.fromAge(customer.age(), ageConfig)));

        // Count tickets by type
        Map<TicketType, Integer> ticketCounts = customersByType.entrySet().stream()
            .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue().size()));

        // Calculate ticket summaries
        List<TicketSummary> ticketSummaries = customersByType.entrySet().stream()
            .map(entry -> {
                TicketType ticketType = entry.getKey();
                int quantity = entry.getValue().size();
                BigDecimal totalCost = priceCalculator.calculatePriceWithDiscount(ticketType, quantity, ticketCounts);
                return new TicketSummary(ticketType.name(), quantity, totalCost);
            })
            .sorted(Comparator.comparing(TicketSummary::ticketType)) // Sort alphabetically
            .toList();

        // Calculate total cost
        BigDecimal totalCost = ticketSummaries.stream()
            .map(TicketSummary::totalCost)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new TransactionResponse(request.transactionId(), ticketSummaries, totalCost);
    }
}