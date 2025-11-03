package au.com.sportsbet.movietickets.service;

import au.com.sportsbet.movietickets.config.AgeConfiguration;
import au.com.sportsbet.movietickets.model.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TransactionProcessor {

  private static final Logger log = LoggerFactory.getLogger(TransactionProcessor.class);
  private final AgeConfiguration ageConfig;
  private final TicketPriceCalculator priceCalculator;

  public TransactionProcessor(AgeConfiguration ageConfig, TicketPriceCalculator priceCalculator) {
    this.ageConfig = ageConfig;
    this.priceCalculator = priceCalculator;
  }

  /**
   * Processes a movie ticket transaction by calculating costs for all customers.
   *
   * @param request the transaction request containing transaction ID and customer list
   * @return TransactionResponse containing ticket summaries and total cost
   */
  public TransactionResponse processTransaction(TransactionRequest request) {
    validateRequest(request);

    log.info(
        "Processing transaction ID: {} with {} customers",
        request.transactionId(),
        request.customers().size());

    Map<TicketType, List<Customer>> grouped = groupCustomersByTicketType(request.customers());
    log.debug("Grouped customers by ticket type: {}", grouped.keySet());

    Map<TicketType, Integer> ticketCounts = countTickets(grouped);
    log.debug("Ticket counts: {}", ticketCounts);

    List<TicketSummary> summaries = buildSummaries(ticketCounts);

    BigDecimal totalCost =
        summaries.stream().map(TicketSummary::totalCost).reduce(BigDecimal.ZERO, BigDecimal::add);

    summaries.sort(Comparator.comparing(TicketSummary::ticketType));

    log.info(
        "Transaction ID: {} processed successfully - Total cost: ${}, {} ticket types",
        request.transactionId(),
        totalCost,
        summaries.size());

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
              return new TicketSummary(type.getDisplayName(), quantity, totalCost);
            })
        .collect(Collectors.toList());
  }
}
