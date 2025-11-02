package au.com.sportsbet.movietickets.service;

import au.com.sportsbet.movietickets.config.AgeConfiguration;
import au.com.sportsbet.movietickets.config.PricingConfiguration;
import au.com.sportsbet.movietickets.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TransactionProcessorTest {

    private TransactionProcessor processor;
    private AgeConfiguration ageConfig;
    private PricingConfiguration pricingConfig;

    @BeforeEach
    void setUp() {
        // Setup age configuration
        ageConfig = new AgeConfiguration();
        ageConfig.setChildrenMax(10);
        ageConfig.setTeenMin(11);
        ageConfig.setTeenMax(17);
        ageConfig.setAdultMin(18);
        ageConfig.setAdultMax(64);
        ageConfig.setSeniorMin(65);

        // Setup pricing configuration
        pricingConfig = new PricingConfiguration();
        pricingConfig.setAdult(BigDecimal.valueOf(25.00));
        pricingConfig.setTeen(BigDecimal.valueOf(12.00));
        pricingConfig.setChildren(BigDecimal.valueOf(5.00));
        pricingConfig.setSeniorDiscountRate(BigDecimal.valueOf(0.30));
        pricingConfig.setChildrenGroupDiscountRate(BigDecimal.valueOf(0.25));
        pricingConfig.setChildrenGroupThreshold(3);

        TicketPriceCalculator calculator = new TicketPriceCalculator(pricingConfig);
        processor = new TransactionProcessor(ageConfig, calculator);
    }

    @Test
    void basicHappyPath_CustomersSpanningAll4AgeRanges() {
        // Given: Customers of all 4 age ranges
        TransactionRequest request = new TransactionRequest(1,
            List.of(
                new Customer("Child", 5),      // CHILDREN
                new Customer("Teen", 15),      // TEEN
                new Customer("Adult", 30),     // ADULT
                new Customer("Senior", 70)     // SENIOR
            )
        );

        // When
        TransactionResponse response = processor.processTransaction(request);

        // Then
        assertEquals(1, response.transactionId());
        assertEquals(4, response.tickets().size());
        assertEquals(new BigDecimal("59.50"), response.totalCost()); // 5.00 + 12.00 + 25.00 + 17.50

        // Verify ticket summaries are present for all types
        var ticketTypes = response.tickets().stream()
            .map(TicketSummary::ticketType)
            .toList();
        assertTrue(ticketTypes.contains("CHILDREN"));
        assertTrue(ticketTypes.contains("TEEN"));
        assertTrue(ticketTypes.contains("ADULT"));
        assertTrue(ticketTypes.contains("SENIOR"));
    }

    @Test
    void childrenDiscount_3OrMoreChildrenShouldTriggerDiscount() {
        // Given: 3 children + 1 adult (should trigger 25% discount on children)
        TransactionRequest request = new TransactionRequest(1,
            List.of(
                new Customer("Child1", 5),
                new Customer("Child2", 6),
                new Customer("Child3", 7),
                new Customer("Adult", 30)
            )
        );

        // When
        TransactionResponse response = processor.processTransaction(request);

        // Then
        assertEquals(2, response.tickets().size());
        assertEquals(new BigDecimal("36.25"), response.totalCost()); // 11.25 (children) + 25.00 (adult)

        // Verify children got discount (3.75 each instead of 5.00)
        TicketSummary childrenSummary = response.tickets().stream()
            .filter(t -> "CHILDREN".equals(t.ticketType()))
            .findFirst().orElseThrow();
        assertEquals(3, childrenSummary.quantity());
        assertEquals(new BigDecimal("11.25"), childrenSummary.totalCost()); // 3 × 3.75
    }

    @Test
    void boundaryAges_10_11_17_18_64_65MapCorrectlyToTypes() {
        // Given: Boundary age customers
        TransactionRequest request = new TransactionRequest(1,
            List.of(
                new Customer("Boundary10", 10),  // CHILDREN (≤10)
                new Customer("Boundary11", 11),  // TEEN (≥11, ≤17)
                new Customer("Boundary17", 17),  // TEEN (≥11, ≤17)
                new Customer("Boundary18", 18),  // ADULT (≥18, ≤64)
                new Customer("Boundary64", 64),  // ADULT (≥18, ≤64)
                new Customer("Boundary65", 65)   // SENIOR (≥65)
            )
        );

        // When
        TransactionResponse response = processor.processTransaction(request);

        // Then
        assertEquals(4, response.tickets().size());
        assertEquals(new BigDecimal("96.50"), response.totalCost()); // 5.00 + 24.00 + 50.00 + 17.50

        // Verify correct grouping by boundary ages
        var ticketMap = response.tickets().stream()
            .collect(java.util.stream.Collectors.toMap(
                TicketSummary::ticketType,
                TicketSummary::quantity
            ));

        assertEquals(1, ticketMap.get("CHILDREN"));
        assertEquals(2, ticketMap.get("TEEN"));
        assertEquals(2, ticketMap.get("ADULT"));
        assertEquals(1, ticketMap.get("SENIOR"));
    }

    @Test
    void emptyNullCustomers_ThrowsIllegalArgumentException() {
        // Test null request
        IllegalArgumentException nullRequestException = assertThrows(
            IllegalArgumentException.class,
            () -> processor.processTransaction(null)
        );
        assertEquals("Transaction request cannot be null", nullRequestException.getMessage());

        // Test null customers list
        TransactionRequest requestWithNullCustomers = new TransactionRequest(1, null);
        IllegalArgumentException nullCustomersException = assertThrows(
            IllegalArgumentException.class,
            () -> processor.processTransaction(requestWithNullCustomers)
        );
        assertEquals("Customer list cannot be null", nullCustomersException.getMessage());
    }

    @Test
    void alphabeticalOrderCheck_TicketSummariesReturnedSortedCorrectly() {
        // Given: Customers that will create all ticket types
        TransactionRequest request = new TransactionRequest(1,
            List.of(
                new Customer("Senior", 70),    // SENIOR
                new Customer("Child", 5),      // CHILDREN
                new Customer("Teen", 15),      // TEEN
                new Customer("Adult", 30)      // ADULT
            )
        );

        // When
        TransactionResponse response = processor.processTransaction(request);

        // Then: Verify alphabetical order (ADULT, CHILDREN, SENIOR, TEEN)
        List<TicketSummary> tickets = response.tickets();
        assertEquals("ADULT", tickets.get(0).ticketType());
        assertEquals("CHILDREN", tickets.get(1).ticketType());
        assertEquals("SENIOR", tickets.get(2).ticketType());
        assertEquals("TEEN", tickets.get(3).ticketType());
    }

    @Test
    void shouldHandleSingleCustomerPerType() {
        // Given: One customer of each type
        TransactionRequest request = new TransactionRequest(1,
            List.of(
                new Customer("Child", 8),
                new Customer("Teen", 14),
                new Customer("Adult", 25),
                new Customer("Senior", 75)
            )
        );

        // When
        TransactionResponse response = processor.processTransaction(request);

        // Then
        assertEquals(4, response.tickets().size());
        assertEquals(new BigDecimal("59.50"), response.totalCost()); // 5.00 + 12.00 + 25.00 + 17.50
    }

    @Test
    void shouldHandleMultipleCustomersSameType() {
        // Given: Multiple customers of same type
        TransactionRequest request = new TransactionRequest(1,
            List.of(
                new Customer("Adult1", 25),
                new Customer("Adult2", 35),
                new Customer("Adult3", 45)
            )
        );

        // When
        TransactionResponse response = processor.processTransaction(request);

        // Then
        assertEquals(1, response.tickets().size());
        assertEquals("ADULT", response.tickets().get(0).ticketType());
        assertEquals(3, response.tickets().get(0).quantity());
        assertEquals(new BigDecimal("75.00"), response.tickets().get(0).totalCost()); // 3 × 25.00
        assertEquals(new BigDecimal("75.00"), response.totalCost());
    }

    @Test
    void testProcessTransaction_SampleFromPDF() {
        // Test case from PDF sample input
        TransactionRequest request = new TransactionRequest(1,
            List.of(
                new Customer("John Smith", 70),  // Senior
                new Customer("Jane Doe", 5),     // Children
                new Customer("Bob Doe", 6)       // Children
            )
        );

        TransactionResponse response = processor.processTransaction(request);

        assertEquals(1, response.transactionId());
        assertEquals(2, response.tickets().size());

        // Find Children and Senior tickets
        TicketSummary childrenTicket = response.tickets().stream()
            .filter(t -> "CHILDREN".equals(t.ticketType()))
            .findFirst().orElseThrow();
        TicketSummary seniorTicket = response.tickets().stream()
            .filter(t -> "SENIOR".equals(t.ticketType()))
            .findFirst().orElseThrow();

        assertEquals(2, childrenTicket.quantity());
        assertEquals(new BigDecimal("10.00"), childrenTicket.totalCost()); // 2 × 5.00 (no discount for 2)
        assertEquals(1, seniorTicket.quantity());
        assertEquals(new BigDecimal("17.50"), seniorTicket.totalCost()); // 1 × 17.50
        assertEquals(new BigDecimal("27.50"), response.totalCost()); // 10.00 + 17.50
    }
}