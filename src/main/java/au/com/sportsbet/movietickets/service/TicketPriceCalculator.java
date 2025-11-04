package au.com.sportsbet.movietickets.service;

import au.com.sportsbet.movietickets.config.PricingConfiguration;
import au.com.sportsbet.movietickets.model.TicketType;
import au.com.sportsbet.movietickets.service.discount.DiscountPolicy;
import au.com.sportsbet.movietickets.service.discount.PricingContext;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TicketPriceCalculator {

  private static final Logger log = LoggerFactory.getLogger(TicketPriceCalculator.class);

  private final PricingConfiguration pricingConfig;
  private final List<DiscountPolicy> discountPolicies;

  @Autowired
  public TicketPriceCalculator(
      PricingConfiguration pricingConfig, List<DiscountPolicy> discountPolicies) {
    this.pricingConfig = pricingConfig;
    this.discountPolicies =
        new ArrayList<>(discountPolicies != null ? discountPolicies : List.of());
    this.discountPolicies.sort(Comparator.comparingInt(DiscountPolicy::order));
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
    BigDecimal unitPrice = calculateBasePrice(ticketType);

    // Build pricing context once per calculation
    PricingContext ctx = new PricingContext(ticketCounts, pricingConfig);
    // Apply discount policies in order, scaling after each policy to maintain money precision
    for (DiscountPolicy policy : discountPolicies) {
      if (policy.supports(ticketType)) {
        BigDecimal afterPolicy = policy.applyUnitPrice(unitPrice, ticketType, ctx);
        unitPrice = MoneyUtils.scale(afterPolicy);
      }
    }

    BigDecimal totalCost = MoneyUtils.scale(unitPrice.multiply(BigDecimal.valueOf(quantity)));
    log.debug(
        "Calculated price for {} {} tickets: ${} (unit after discounts: ${})",
        quantity,
        ticketType,
        totalCost,
        unitPrice);

    return totalCost;
  }

  private BigDecimal calculateBasePrice(TicketType ticketType) {
    return switch (ticketType) {
      case ADULT -> pricingConfig.adultPrice();
      case SENIOR -> {
        // Senior gets discount off adult price
        BigDecimal adultPrice = pricingConfig.adultPrice();
        BigDecimal discount = adultPrice.multiply(pricingConfig.seniorDiscountRate());
        yield MoneyUtils.scale(adultPrice.subtract(discount));
      }
      case TEEN -> pricingConfig.teenPrice();
      case CHILDREN -> pricingConfig.childrenPrice();
    };
  }
}
