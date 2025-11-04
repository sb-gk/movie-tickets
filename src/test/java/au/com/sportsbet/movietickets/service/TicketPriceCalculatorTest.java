package au.com.sportsbet.movietickets.service;

import static au.com.sportsbet.movietickets.util.TestBuilders.pricingConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;

import au.com.sportsbet.movietickets.config.PricingConfiguration;
import au.com.sportsbet.movietickets.model.TicketType;
import au.com.sportsbet.movietickets.service.discount.ChildrenGroupDiscountPolicy;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TicketPriceCalculatorTest {

  private TicketPriceCalculator calculator;

  @BeforeEach
  void setUp() {
    PricingConfiguration config = pricingConfig();
    calculator = new TicketPriceCalculator(config, List.of(new ChildrenGroupDiscountPolicy()));
  }

  @Test
  void testCalculatePriceWithDiscount_NoChildrenDiscount() {
    Map<TicketType, Integer> ticketCounts = Map.of(TicketType.CHILDREN, 2);
    BigDecimal result = calculator.calculatePriceWithDiscount(TicketType.CHILDREN, 2, ticketCounts);
    assertEquals(new BigDecimal("10.00"), result); // 5.00 * 2
  }

  @Test
  void testCalculatePriceWithDiscount_WithChildrenDiscount() {
    Map<TicketType, Integer> ticketCounts = Map.of(TicketType.CHILDREN, 3);
    BigDecimal result = calculator.calculatePriceWithDiscount(TicketType.CHILDREN, 3, ticketCounts);
    assertEquals(new BigDecimal("11.25"), result); // (5.00 * 0.75) * 3 = 3.75 * 3
  }

  @Test
  void testCalculatePriceWithDiscount_AdultNoDiscount() {
    Map<TicketType, Integer> ticketCounts = Map.of(TicketType.ADULT, 1);
    BigDecimal result = calculator.calculatePriceWithDiscount(TicketType.ADULT, 1, ticketCounts);
    assertEquals(new BigDecimal("25.00"), result);
  }

  @Test
  void testCalculatePriceWithDiscount_SeniorNoDiscount() {
    Map<TicketType, Integer> ticketCounts = Map.of(TicketType.SENIOR, 1);
    BigDecimal result = calculator.calculatePriceWithDiscount(TicketType.SENIOR, 1, ticketCounts);
    assertEquals(new BigDecimal("17.50"), result);
  }
}
