package au.com.sportsbet.movietickets.service;

import static org.junit.jupiter.api.Assertions.*;

import au.com.sportsbet.movietickets.config.PricingConfiguration;
import au.com.sportsbet.movietickets.model.TicketType;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TicketPriceCalculatorTest {

  private TicketPriceCalculator calculator;
  private PricingConfiguration config;

  @BeforeEach
  void setUp() {
    config = new PricingConfiguration();
    config.setAdultPrice(new BigDecimal("25.00"));
    config.setTeenPrice(new BigDecimal("12.00"));
    config.setChildrenPrice(new BigDecimal("5.00"));
    config.setSeniorDiscountRate(new BigDecimal("0.30"));
    config.setChildrenGroupDiscountRate(new BigDecimal("0.25"));
    config.setChildrenGroupThreshold(3);

    calculator = new TicketPriceCalculator(config);
  }

  @Test
  void testCalculateBasePrice_Adult() {
    assertEquals(new BigDecimal("25.00"), calculator.calculateBasePrice(TicketType.ADULT));
  }

  @Test
  void testCalculateBasePrice_Teen() {
    assertEquals(new BigDecimal("12.00"), calculator.calculateBasePrice(TicketType.TEEN));
  }

  @Test
  void testCalculateBasePrice_Children() {
    assertEquals(new BigDecimal("5.00"), calculator.calculateBasePrice(TicketType.CHILDREN));
  }

  @Test
  void testCalculateBasePrice_Senior() {
    // Senior gets 30% discount off adult price: 25.00 * 0.70 = 17.50
    assertEquals(new BigDecimal("17.50"), calculator.calculateBasePrice(TicketType.SENIOR));
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
