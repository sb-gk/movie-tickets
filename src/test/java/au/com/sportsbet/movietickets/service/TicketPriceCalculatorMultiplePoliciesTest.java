package au.com.sportsbet.movietickets.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import au.com.sportsbet.movietickets.config.PricingConfiguration;
import au.com.sportsbet.movietickets.model.TicketType;
import au.com.sportsbet.movietickets.service.discount.ChildrenGroupDiscountPolicy;
import au.com.sportsbet.movietickets.service.discount.DiscountPolicy;
import au.com.sportsbet.movietickets.service.discount.PricingContext;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Validates that multiple discount policies are composed (all applied), and the final per-unit
 * price after all policies is multiplied by quantity.
 */
class TicketPriceCalculatorMultiplePoliciesTest {

  @Test
  void multiplePoliciesAreComposed_Matinee10PercentPlusChildrenGroup25Percent() {
    // Pricing: Adult 25, Teen 12, Children 5; Children group discount 25% for >=3
    PricingConfiguration cfg =
        new PricingConfiguration(
            new BigDecimal("25.00"),
            new BigDecimal("12.00"),
            new BigDecimal("5.00"),
            new BigDecimal("0.30"),
            new BigDecimal("0.25"),
            3);

    // Compose two policies: existing Children group policy AND a hypothetical "Matinee 10% off"
    List<DiscountPolicy> policies =
        List.of(new ChildrenGroupDiscountPolicy(), new TenPercentOffAllPolicy());

    TicketPriceCalculator calculator = new TicketPriceCalculator(cfg, policies);

    // 3 children => children group discount applies
    Map<TicketType, Integer> counts = Map.of(TicketType.CHILDREN, 3);

    // Base unit: 5.00
    // After children group discount (25% off): 5.00 * 0.75 = 3.75
    // After matinee 10% off: 3.75 * 0.90 = 3.375 -> scaled HALF_UP to 3.38
    // Total for qty=3: 3 * 3.38 = 10.14
    BigDecimal total = calculator.calculatePriceWithDiscount(TicketType.CHILDREN, 3, counts);
    assertEquals(new BigDecimal("10.14"), total);
  }

  /**
   * Simple test policy that applies a flat 10% off to any ticket type. Used here to demonstrate
   * composition with the existing children group discount.
   */
  static class TenPercentOffAllPolicy implements DiscountPolicy {
    @Override
    public BigDecimal applyUnitPrice(
        BigDecimal baseUnitPrice, TicketType ticketType, PricingContext context) {
      return baseUnitPrice.multiply(new BigDecimal("0.90"));
    }

    @Override
    public int order() {
      return 1; // order after the children policy (which defaults to 0)
    }
  }
}
