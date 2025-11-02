package au.com.sportsbet.movietickets.service;

import au.com.sportsbet.movietickets.config.AgeConfiguration;
import au.com.sportsbet.movietickets.model.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TransactionProcessor {

    private final AgeConfiguration ageConfig;
    private final TicketPriceCalculator priceCalculator;

    public TransactionProcessor(AgeConfiguration ageConfig, TicketPriceCalculator priceCalculator) {
        this.ageConfig = ageConfig;
        this.priceCalculator = priceCalculator;
    }

    /**
     * Processes a transaction request and returns the calculated ticket summary and total.
     */
    public TransactionResponse processTransaction(TransactionRequest request) {
        validateRequest(request);

        // 1️⃣ Classify customers by ticket type
        Map<TicketType, List<Customer>> grouped = groupCustomersByTicketType(request.customers());

        // 2️⃣ Count tickets per type
        Map<TicketType, Integer> ticketCounts = countTickets(grouped);

        // 3️⃣ Build per-type summaries
        List<TicketSummary> summaries = buildSummaries(ticketCounts);

        // 4️⃣ Calculate overall total
        BigDecimal totalCost = summaries.stream()
                .map(TicketSummary::totalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 5️⃣ Sort summaries alphabetically by ticketType (as per requirements)
        summaries.sort(Comparator.comparing(TicketSummary::ticketType));

        return new TransactionResponse(request.transactionId(), summaries, totalCost);
    }

    // ---------------------------- private helpers ----------------------------

    private void validateRequest(TransactionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Transaction request cannot be null");
        }
        if (request.customers() == null) {
            throw new IllegalArgumentException("Customer list cannot be null");
        }
    }

    private Map<TicketType, List<Customer>> groupCustomersByTicketType(List<Customer> customers) {
        return customers.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(c -> TicketType.fromAge(c.age(), ageConfig)));
    }

    private Map<TicketType, Integer> countTickets(Map<TicketType, List<Customer>> grouped) {
        return grouped.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().size()
                ));
    }

    private List<TicketSummary> buildSummaries(Map<TicketType, Integer> ticketCounts) {
        return ticketCounts.entrySet().stream()
                .map(e -> {
                    TicketType type = e.getKey();
                    int quantity = e.getValue();
                    BigDecimal totalCost = priceCalculator.calculatePriceWithDiscount(type, quantity, ticketCounts);
                    return new TicketSummary(type.name(), quantity, totalCost);
                })
                .collect(Collectors.toList());
    }
}
