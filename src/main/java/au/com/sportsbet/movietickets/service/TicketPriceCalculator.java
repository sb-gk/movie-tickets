package au.com.sportsbet.movietickets.service;

import au.com.sportsbet.movietickets.config.PricingConfiguration;
import au.com.sportsbet.movietickets.model.TicketType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class TicketPriceCalculator {

  public static final int SCALE = 2;
  public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

  private final PricingConfiguration pricingConfig;

  public TicketPriceCalculator(PricingConfiguration pricingConfig) {
    this.pricingConfig = pricingConfig;
  }

  public BigDecimal calculatePriceWithDiscount(
      TicketType ticketType, int quantity, Map<TicketType, Integer> ticketCounts) {
    BigDecimal basePrice = calculateBasePrice(ticketType);

    // Apply group discount for children if threshold is met
    if (ticketType == TicketType.CHILDREN && qualifiesForChildrenDiscount(ticketCounts)) {
      basePrice = applyDiscount(basePrice, pricingConfig.getChildrenGroupDiscountRate());
    }

    return scaleBd(basePrice.multiply(BigDecimal.valueOf(quantity)));
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
