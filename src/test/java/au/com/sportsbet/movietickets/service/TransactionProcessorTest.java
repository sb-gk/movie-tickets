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
        processor = new TransactionProcessor(calculator, ageConfig);
    }

    @Test
    void testProcessTransaction_SingleAdult() {
        TransactionRequest request = new TransactionRequest(1,
            List.of(new Customer("John Doe", 30))
        );

        TransactionResponse response = processor.processTransaction(request);

        assertEquals(1, response.transactionId());
        assertEquals(1, response.tickets().size());
        assertEquals("ADULT", response.tickets().get(0).ticketType());
        assertEquals(1, response.tickets().get(0).quantity());
        assertEquals(new BigDecimal("25.00"), response.tickets().get(0).totalCost());
        assertEquals(new BigDecimal("25.00"), response.totalCost());
    }

    @Test
    void testProcessTransaction_SingleSenior() {
        TransactionRequest request = new TransactionRequest(1,
            List.of(new Customer("Jane Smith", 70))
        );

        TransactionResponse response = processor.processTransaction(request);

        assertEquals(1, response.transactionId());
        assertEquals(1, response.tickets().size());
        assertEquals("SENIOR", response.tickets().get(0).ticketType());
        assertEquals(1, response.tickets().get(0).quantity());
        assertEquals(new BigDecimal("17.50"), response.tickets().get(0).totalCost());
        assertEquals(new BigDecimal("17.50"), response.totalCost());
    }

    @Test
    void testProcessTransaction_ChildrenGroupDiscount() {
        TransactionRequest request = new TransactionRequest(1,
            List.of(
                new Customer("Child1", 5),
                new Customer("Child2", 6),
                new Customer("Child3", 7)
            )
        );

        TransactionResponse response = processor.processTransaction(request);

        assertEquals(1, response.transactionId());
        assertEquals(1, response.tickets().size());
        assertEquals("CHILDREN", response.tickets().get(0).ticketType());
        assertEquals(3, response.tickets().get(0).quantity());
        assertEquals(new BigDecimal("11.25"), response.tickets().get(0).totalCost()); // 3.75 * 3
        assertEquals(new BigDecimal("11.25"), response.totalCost());
    }

    @Test
    void testProcessTransaction_MixedTypes() {
        TransactionRequest request = new TransactionRequest(1,
            List.of(
                new Customer("Adult", 30),
                new Customer("Senior", 70),
                new Customer("Teen", 15),
                new Customer("Child", 8)
            )
        );

        TransactionResponse response = processor.processTransaction(request);

        assertEquals(1, response.transactionId());
        assertEquals(4, response.tickets().size());

        // Check total cost: 25.00 + 17.50 + 12.00 + 5.00 = 59.50
        assertEquals(new BigDecimal("59.50"), response.totalCost());

        // Check individual tickets are sorted alphabetically
        List<TicketSummary> tickets = response.tickets();
        assertEquals("ADULT", tickets.get(0).ticketType());
        assertEquals("CHILDREN", tickets.get(1).ticketType());
        assertEquals("SENIOR", tickets.get(2).ticketType());
        assertEquals("TEEN", tickets.get(3).ticketType());
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
        assertEquals(new BigDecimal("10.00"), childrenTicket.totalCost()); // 5.00 * 2
        assertEquals(1, seniorTicket.quantity());
        assertEquals(new BigDecimal("17.50"), seniorTicket.totalCost());
        assertEquals(new BigDecimal("27.50"), response.totalCost());
    }
}