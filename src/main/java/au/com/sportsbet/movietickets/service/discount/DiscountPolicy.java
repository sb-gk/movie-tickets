package au.com.sportsbet.movietickets.service.discount;

import au.com.sportsbet.movietickets.model.TicketType;
import java.math.BigDecimal;

/**
 * Strategy for applying a discount to a base per-unit ticket price. Implementations should be
 * side-effect free and return a new price value.
 */
public interface DiscountPolicy {

  /**
   * Apply this discount to the given baseUnitPrice for a single ticket.
   *
   * @param baseUnitPrice the current per-unit price before this policy (scale/rounding handled by
   *     caller)
   * @param ticketType the ticket type being priced
   * @param context pricing context (counts, configuration, future metadata)
   * @return the discounted per-unit price (may be unchanged if no discount applies)
   */
  BigDecimal applyUnitPrice(
      BigDecimal baseUnitPrice, TicketType ticketType, PricingContext context);

  /** Whether this policy applies to a given ticket type. Defaults to true. */
  default boolean supports(TicketType ticketType) {
    return true;
  }

  /** Optional ordering for pipelines. Lower values execute earlier. */
  default int order() {
    return 0;
  }
}
