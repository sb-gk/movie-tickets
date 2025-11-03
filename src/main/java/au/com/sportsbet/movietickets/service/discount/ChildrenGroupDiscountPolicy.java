package au.com.sportsbet.movietickets.service.discount;

import au.com.sportsbet.movietickets.model.TicketType;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

/**
 * Applies the group discount for Children tickets when the threshold is met. Logic equivalent to
 * the previous inline rule in the calculator.
 */
@Component
public class ChildrenGroupDiscountPolicy implements DiscountPolicy {

  @Override
  public boolean supports(TicketType ticketType) {
    return ticketType == TicketType.CHILDREN;
  }

  @Override
  public BigDecimal applyUnitPrice(
      BigDecimal baseUnitPrice, TicketType ticketType, PricingContext context) {

    int childCount = context.countOf(TicketType.CHILDREN);
    var cfg = context.pricingConfiguration();
    if (childCount >= cfg.childrenGroupThreshold()) {
      BigDecimal discount = baseUnitPrice.multiply(cfg.childrenGroupDiscountRate());
      return baseUnitPrice.subtract(discount);
    }
    return baseUnitPrice;
  }

  @Override
  public int order() {
    // Default order; adjust if additional policies require specific sequencing
    return 0;
  }
}
