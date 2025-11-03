package au.com.sportsbet.movietickets.service.discount;

import au.com.sportsbet.movietickets.config.PricingConfiguration;
import au.com.sportsbet.movietickets.model.TicketType;
import java.util.Map;

/**
 * Context provided to discount policies. Encapsulates values and helpers that discounts may need
 * today (counts, pricing configuration) and can be extended safely later (e.g., line quantity,
 * transaction/channel metadata, calendar, etc.).
 */
public record PricingContext(
    Map<TicketType, Integer> ticketCounts, PricingConfiguration pricingConfiguration) {

  /** Convenience accessor for a ticket type count in this transaction. */
  public int countOf(TicketType type) {
    return ticketCounts != null ? ticketCounts.getOrDefault(type, 0) : 0;
  }
}
