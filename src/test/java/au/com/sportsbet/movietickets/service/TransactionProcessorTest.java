package au.com.sportsbet.movietickets.service;

import static au.com.sportsbet.movietickets.util.TestBuilders.ageConfig;
import static au.com.sportsbet.movietickets.util.TestBuilders.assertTicketSummary;
import static au.com.sportsbet.movietickets.util.TestBuilders.assertTicketTypes;
import static au.com.sportsbet.movietickets.util.TestBuilders.assertTotalCost;
import static au.com.sportsbet.movietickets.util.TestBuilders.pricingConfig;
import static au.com.sportsbet.movietickets.util.TestBuilders.request;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import au.com.sportsbet.movietickets.model.TicketSummary;
import au.com.sportsbet.movietickets.model.TransactionRequest;
import au.com.sportsbet.movietickets.model.TransactionResponse;
import au.com.sportsbet.movietickets.service.discount.ChildrenGroupDiscountPolicy;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TransactionProcessorTest {

  private TransactionProcessor processor;

  @BeforeEach
  void setUp() {
    var pricingConfig = pricingConfig();
    var ageConfig = ageConfig();
    TicketPriceCalculator calculator =
        new TicketPriceCalculator(pricingConfig, List.of(new ChildrenGroupDiscountPolicy()));
    processor = new TransactionProcessor(ageConfig, calculator);
  }

  @Test
  void basicHappyPath_CustomersSpanningAll4AgeRanges() {
    // Given: Customers of all 4 age ranges
    TransactionRequest request =
        request()
            .withTransactionId(1)
            .addChild(5) // CHILDREN
            .addTeen(15) // TEEN
            .addAdult(30) // ADULT
            .addSenior(70) // SENIOR
            .build();

    // When
    TransactionResponse response = processor.processTransaction(request);

    // Then
    assertEquals(1, response.transactionId());
    assertEquals(4, response.tickets().size());
    assertTotalCost(response, new BigDecimal("59.50")); // 5.00 + 12.00 + 25.00 + 17.50

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
        request().withTransactionId(1).addChild(5).addChild(6).addChild(7).addAdult(30).build();

    // When
    TransactionResponse response = processor.processTransaction(request);

    // Then
    assertEquals(2, response.tickets().size());
    assertTotalCost(response, new BigDecimal("36.25")); // 11.25 (children) + 25.00 (adult)

    // Verify children got discount (3.75 each instead of 5.00)
    assertTicketSummary(response, "Children", 3, new BigDecimal("11.25")); // 3 × 3.75
  }

  @Test
  void boundaryAges_10_11_17_18_64_65MapCorrectlyToTypes() {
    // Given: Boundary age customers
    TransactionRequest request =
        request()
            .withTransactionId(1)
            .addChild(10) // CHILDREN (≤10)
            .addTeen(11) // TEEN (≥11, ≤17)
            .addTeen(17) // TEEN (≥11, ≤17)
            .addAdult(18) // ADULT (≥18, ≤64)
            .addAdult(64) // ADULT (≥18, ≤64)
            .addSenior(65) // SENIOR (≥65)
            .build();

    // When
    TransactionResponse response = processor.processTransaction(request);

    // Then
    assertEquals(4, response.tickets().size());
    assertTotalCost(response, new BigDecimal("96.50")); // 5.00 + 24.00 + 50.00 + 17.50

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
        request()
            .withTransactionId(1)
            .addSenior(70) // SENIOR
            .addChild(5) // CHILDREN
            .addTeen(15) // TEEN
            .addAdult(30) // ADULT
            .build();

    // When
    TransactionResponse response = processor.processTransaction(request);

    // Then: Verify alphabetical order (Adult, Children, Senior, Teen)
    assertTicketTypes(response, "Adult", "Children", "Senior", "Teen");
  }

  @Test
  void shouldHandleSingleCustomerPerType() {
    // Given: One customer of each type
    TransactionRequest request =
        request().withTransactionId(1).addChild(8).addTeen(14).addAdult(25).addSenior(75).build();

    // When
    TransactionResponse response = processor.processTransaction(request);

    // Then
    assertEquals(4, response.tickets().size());
    assertTotalCost(response, new BigDecimal("59.50")); // 5.00 + 12.00 + 25.00 + 17.50
  }

  @Test
  void shouldHandleMultipleCustomersSameType() {
    // Given: Multiple customers of same type
    TransactionRequest request =
        request().withTransactionId(1).addAdult(25).addAdult(35).addAdult(45).build();

    // When
    TransactionResponse response = processor.processTransaction(request);

    // Then
    assertEquals(1, response.tickets().size());
    assertEquals("Adult", response.tickets().get(0).ticketType());
    assertEquals(3, response.tickets().get(0).quantity());
    assertEquals(new BigDecimal("75.00"), response.tickets().get(0).totalCost()); // 3 × 25.00
    assertTotalCost(response, new BigDecimal("75.00"));
  }

  @Test
  void testProcessTransaction_SampleFromPDF() {
    // Test case from PDF sample input
    TransactionRequest request =
        request()
            .withTransactionId(1)
            .addSenior(70) // Senior
            .addChild(5) // Children
            .addChild(6) // Children
            .build();

    TransactionResponse response = processor.processTransaction(request);

    assertEquals(1, response.transactionId());
    assertEquals(2, response.tickets().size());

    // Find Children and Senior tickets
    assertTicketSummary(
        response, "Children", 2, new BigDecimal("10.00")); // 2 × 5.00 (no discount for 2)
    assertTicketSummary(response, "Senior", 1, new BigDecimal("17.50")); // 1 × 17.50
    assertTotalCost(response, new BigDecimal("27.50")); // 10.00 + 17.50
  }
}
