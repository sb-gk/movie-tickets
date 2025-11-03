package au.com.sportsbet.movietickets.service;

import au.com.sportsbet.movietickets.config.AgeConfiguration;
import au.com.sportsbet.movietickets.model.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class TransactionProcessor {

  private final AgeConfiguration ageConfig;
  private final TicketPriceCalculator priceCalculator;

  public TransactionProcessor(AgeConfiguration ageConfig, TicketPriceCalculator priceCalculator) {
    this.ageConfig = ageConfig;
    this.priceCalculator = priceCalculator;
  }

  public TransactionResponse processTransaction(TransactionRequest request) {
    validateRequest(request);

    Map<TicketType, List<Customer>> grouped = groupCustomersByTicketType(request.customers());

    Map<TicketType, Integer> ticketCounts = countTickets(grouped);

    List<TicketSummary> summaries = buildSummaries(ticketCounts);

    BigDecimal totalCost =
        summaries.stream().map(TicketSummary::totalCost).reduce(BigDecimal.ZERO, BigDecimal::add);

    // 5️⃣ Sort summaries alphabetically by ticketType (as per requirements)
    summaries.sort(Comparator.comparing(TicketSummary::ticketType));

    return new TransactionResponse(request.transactionId(), summaries, totalCost);
  }

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
        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().size()));
  }

  private List<TicketSummary> buildSummaries(Map<TicketType, Integer> ticketCounts) {
    return ticketCounts.entrySet().stream()
        .map(
            e -> {
              TicketType type = e.getKey();
              int quantity = e.getValue();
              BigDecimal totalCost =
                  priceCalculator.calculatePriceWithDiscount(type, quantity, ticketCounts);
              return new TicketSummary(type.name(), quantity, totalCost);
            })
        .collect(Collectors.toList());
  }
}
