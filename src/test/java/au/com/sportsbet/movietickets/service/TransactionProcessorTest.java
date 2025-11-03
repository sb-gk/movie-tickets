package au.com.sportsbet.movietickets.service;

import static org.junit.jupiter.api.Assertions.*;

import au.com.sportsbet.movietickets.config.AgeConfiguration;
import au.com.sportsbet.movietickets.config.PricingConfiguration;
import au.com.sportsbet.movietickets.model.*;
import au.com.sportsbet.movietickets.service.discount.ChildrenGroupDiscountPolicy;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TransactionProcessorTest {

  private TransactionProcessor processor;
  private AgeConfiguration ageConfig;
  private PricingConfiguration pricingConfig;

  @BeforeEach
  void setUp() {
    // Setup age configuration (constructor-bound record)
    ageConfig =
        new AgeConfiguration(
            10, // childrenMax
            11, // teenMin
            17, // teenMax
            18, // adultMin
            64, // adultMax
            65 // seniorMin
            );

    // Setup pricing configuration (constructor-bound record)
    pricingConfig =
        new PricingConfiguration(
            BigDecimal.valueOf(25.00), // adultPrice
            BigDecimal.valueOf(12.00), // teenPrice
            BigDecimal.valueOf(5.00), // childrenPrice
            BigDecimal.valueOf(0.30), // seniorDiscountRate
            BigDecimal.valueOf(0.25), // childrenGroupDiscountRate
            3 // childrenGroupThreshold
            );

    TicketPriceCalculator calculator =
        new TicketPriceCalculator(pricingConfig, List.of(new ChildrenGroupDiscountPolicy()));
    processor = new TransactionProcessor(ageConfig, calculator);
  }

  @Test
  void basicHappyPath_CustomersSpanningAll4AgeRanges() {
    // Given: Customers of all 4 age ranges
    TransactionRequest request =
        new TransactionRequest(
            1,
            List.of(
                new Customer("Child", 5), // CHILDREN
                new Customer("Teen", 15), // TEEN
                new Customer("Adult", 30), // ADULT
                new Customer("Senior", 70) // SENIOR
                ));

    // When
    TransactionResponse response = processor.processTransaction(request);

    // Then
    assertEquals(1, response.transactionId());
    assertEquals(4, response.tickets().size());
    assertEquals(new BigDecimal("59.50"), response.totalCost()); // 5.00 + 12.00 + 25.00 + 17.50

    // Verify ticket summaries are present for all types
    var ticketTypes = response.tickets().stream().map(TicketSummary::ticketType).toList();
    assertTrue(ticketTypes.contains("Children"));
    assertTrue(ticketTypes.contains("Teen"));
    assertTrue(ticketTypes.contains("Adult"));
    assertTrue(ticketTypes.contains("Senior"));
  }

  @Test
  void childrenDiscount_3OrMoreChildrenShouldTriggerDiscount() {
    // Given: 3 children + 1 adult (should trigger 25% discount on children)
    TransactionRequest request =
        new TransactionRequest(
            1,
            List.of(
                new Customer("Child1", 5),
                new Customer("Child2", 6),
                new Customer("Child3", 7),
                new Customer("Adult", 30)));

    // When
    TransactionResponse response = processor.processTransaction(request);

    // Then
    assertEquals(2, response.tickets().size());
    assertEquals(new BigDecimal("36.25"), response.totalCost()); // 11.25 (children) + 25.00 (adult)

    // Verify children got discount (3.75 each instead of 5.00)
    TicketSummary childrenSummary =
        response.tickets().stream()
            .filter(t -> "Children".equals(t.ticketType()))
            .findFirst()
            .orElseThrow();
    assertEquals(3, childrenSummary.quantity());
    assertEquals(new BigDecimal("11.25"), childrenSummary.totalCost()); // 3 × 3.75
  }

  @Test
  void boundaryAges_10_11_17_18_64_65MapCorrectlyToTypes() {
    // Given: Boundary age customers
    TransactionRequest request =
        new TransactionRequest(
            1,
            List.of(
                new Customer("Boundary10", 10), // CHILDREN (≤10)
                new Customer("Boundary11", 11), // TEEN (≥11, ≤17)
                new Customer("Boundary17", 17), // TEEN (≥11, ≤17)
                new Customer("Boundary18", 18), // ADULT (≥18, ≤64)
                new Customer("Boundary64", 64), // ADULT (≥18, ≤64)
                new Customer("Boundary65", 65) // SENIOR (≥65)
                ));

    // When
    TransactionResponse response = processor.processTransaction(request);

    // Then
    assertEquals(4, response.tickets().size());
    assertEquals(new BigDecimal("96.50"), response.totalCost()); // 5.00 + 24.00 + 50.00 + 17.50

    // Verify correct grouping by boundary ages
    var ticketMap =
        response.tickets().stream()
            .collect(
                java.util.stream.Collectors.toMap(
                    TicketSummary::ticketType, TicketSummary::quantity));

    assertEquals(1, ticketMap.get("Children"));
    assertEquals(2, ticketMap.get("Teen"));
    assertEquals(2, ticketMap.get("Adult"));
    assertEquals(1, ticketMap.get("Senior"));
  }

  @Test
  void alphabeticalOrderCheck_TicketSummariesReturnedSortedCorrectly() {
    // Given: Customers that will create all ticket types
    TransactionRequest request =
        new TransactionRequest(
            1,
            List.of(
                new Customer("Senior", 70), // SENIOR
                new Customer("Child", 5), // CHILDREN
                new Customer("Teen", 15), // TEEN
                new Customer("Adult", 30) // ADULT
                ));

    // When
    TransactionResponse response = processor.processTransaction(request);

    // Then: Verify alphabetical order (Adult, Children, Senior, Teen)
    List<TicketSummary> tickets = response.tickets();
    assertEquals("Adult", tickets.get(0).ticketType());
    assertEquals("Children", tickets.get(1).ticketType());
    assertEquals("Senior", tickets.get(2).ticketType());
    assertEquals("Teen", tickets.get(3).ticketType());
  }

  @Test
  void shouldHandleSingleCustomerPerType() {
    // Given: One customer of each type
    TransactionRequest request =
        new TransactionRequest(
            1,
            List.of(
                new Customer("Child", 8),
                new Customer("Teen", 14),
                new Customer("Adult", 25),
                new Customer("Senior", 75)));

    // When
    TransactionResponse response = processor.processTransaction(request);

    // Then
    assertEquals(4, response.tickets().size());
    assertEquals(new BigDecimal("59.50"), response.totalCost()); // 5.00 + 12.00 + 25.00 + 17.50
  }

  @Test
  void shouldHandleMultipleCustomersSameType() {
    // Given: Multiple customers of same type
    TransactionRequest request =
        new TransactionRequest(
            1,
            List.of(
                new Customer("Adult1", 25),
                new Customer("Adult2", 35),
                new Customer("Adult3", 45)));

    // When
    TransactionResponse response = processor.processTransaction(request);

    // Then
    assertEquals(1, response.tickets().size());
    assertEquals("Adult", response.tickets().get(0).ticketType());
    assertEquals(3, response.tickets().get(0).quantity());
    assertEquals(new BigDecimal("75.00"), response.tickets().get(0).totalCost()); // 3 × 25.00
    assertEquals(new BigDecimal("75.00"), response.totalCost());
  }

  @Test
  void testProcessTransaction_SampleFromPDF() {
    // Test case from PDF sample input
    TransactionRequest request =
        new TransactionRequest(
            1,
            List.of(
                new Customer("John Smith", 70), // Senior
                new Customer("Jane Doe", 5), // Children
                new Customer("Bob Doe", 6) // Children
                ));

    TransactionResponse response = processor.processTransaction(request);

    assertEquals(1, response.transactionId());
    assertEquals(2, response.tickets().size());

    // Find Children and Senior tickets
    TicketSummary childrenTicket =
        response.tickets().stream()
            .filter(t -> "Children".equals(t.ticketType()))
            .findFirst()
            .orElseThrow();
    TicketSummary seniorTicket =
        response.tickets().stream()
            .filter(t -> "Senior".equals(t.ticketType()))
            .findFirst()
            .orElseThrow();

    assertEquals(2, childrenTicket.quantity());
    assertEquals(
        new BigDecimal("10.00"), childrenTicket.totalCost()); // 2 × 5.00 (no discount for 2)
    assertEquals(1, seniorTicket.quantity());
    assertEquals(new BigDecimal("17.50"), seniorTicket.totalCost()); // 1 × 17.50
    assertEquals(new BigDecimal("27.50"), response.totalCost()); // 10.00 + 17.50
  }
}
