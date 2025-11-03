package au.com.sportsbet.movietickets.service;

import au.com.sportsbet.movietickets.config.PricingConfiguration;
import au.com.sportsbet.movietickets.model.TicketType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TicketPriceCalculator {

  private static final Logger log = LoggerFactory.getLogger(TicketPriceCalculator.class);

  public static final int SCALE = 2;
  public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

  private final PricingConfiguration pricingConfig;

  public TicketPriceCalculator(PricingConfiguration pricingConfig) {
    this.pricingConfig = pricingConfig;
  }

  /**
   * Calculates the total price for a given ticket type and quantity, applying any applicable
   * discounts.
   *
   * @param ticketType the type of ticket (Adult, Senior, Teen, Children)
   * @param quantity the number of tickets of this type
   * @param ticketCounts map of all ticket types and their quantities in the transaction
   * @return the total cost for this ticket type after applying discounts
   */
  public BigDecimal calculatePriceWithDiscount(
      TicketType ticketType, int quantity, Map<TicketType, Integer> ticketCounts) {
    BigDecimal basePrice = calculateBasePrice(ticketType);

    // Apply group discount for children if threshold is met
    if (ticketType == TicketType.CHILDREN && qualifiesForChildrenDiscount(ticketCounts)) {
      log.debug("Applying children group discount for {} tickets", quantity);
      basePrice = applyDiscount(basePrice, pricingConfig.getChildrenGroupDiscountRate());
    }

    BigDecimal totalCost = scaleBd(basePrice.multiply(BigDecimal.valueOf(quantity)));
    log.debug(
        "Calculated price for {} {} tickets: ${} (base: ${})",
        quantity,
        ticketType,
        totalCost,
        basePrice);

    return totalCost;
  }

  private BigDecimal calculateBasePrice(TicketType ticketType) {
    return switch (ticketType) {
      case ADULT -> pricingConfig.getAdultPrice();
      case SENIOR -> {
        // Senior gets discount off adult price
        BigDecimal adultPrice = pricingConfig.getAdultPrice();
        BigDecimal discount = adultPrice.multiply(pricingConfig.getSeniorDiscountRate());
        yield scaleBd(adultPrice.subtract(discount));
      }
      case TEEN -> pricingConfig.getTeenPrice();
      case CHILDREN -> pricingConfig.getChildrenPrice();
    };
  }

  private BigDecimal scaleBd(BigDecimal value) {
    return value.setScale(SCALE, ROUNDING_MODE);
  }

  private boolean qualifiesForChildrenDiscount(Map<TicketType, Integer> ticketCounts) {
    int childCount = ticketCounts.getOrDefault(TicketType.CHILDREN, 0);
    return childCount >= pricingConfig.getChildrenGroupThreshold();
  }

  private BigDecimal applyDiscount(BigDecimal basePrice, BigDecimal discountRate) {
    BigDecimal discount = basePrice.multiply(discountRate);
    basePrice = scaleBd(basePrice.subtract(discount));
    return basePrice;
  }
}
