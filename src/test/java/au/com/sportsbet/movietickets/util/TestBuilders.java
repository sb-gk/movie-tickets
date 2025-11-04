package au.com.sportsbet.movietickets.util;

import au.com.sportsbet.movietickets.config.AgeConfiguration;
import au.com.sportsbet.movietickets.config.PricingConfiguration;
import au.com.sportsbet.movietickets.model.Customer;
import au.com.sportsbet.movietickets.model.TicketSummary;
import au.com.sportsbet.movietickets.model.TransactionRequest;
import au.com.sportsbet.movietickets.model.TransactionResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;

/**
 * Utility class providing builder patterns for creating test data objects and assertion helpers for
 * common test verifications.
 */
public final class TestBuilders {

  private TestBuilders() {
    // Utility class
  }

  // ===== CONFIGURATION BUILDERS =====

  public static AgeConfiguration ageConfig() {
    return new AgeConfiguration(10, 11, 17, 18, 64, 65);
  }

  public static PricingConfiguration pricingConfig() {
    return new PricingConfiguration(
        new BigDecimal("25.00"),
        new BigDecimal("12.00"),
        new BigDecimal("5.00"),
        new BigDecimal("0.30"),
        new BigDecimal("0.25"),
        3);
  }

  // ===== CUSTOMER BUILDER =====

  public static CustomerBuilder customer() {
    return new CustomerBuilder();
  }

  public static class CustomerBuilder {
    private String name = "Test Customer";
    private int age = 30;

    public CustomerBuilder withName(String name) {
      this.name = name;
      return this;
    }

    public CustomerBuilder withAge(int age) {
      this.age = age;
      return this;
    }

    public Customer build() {
      return new Customer(name, age);
    }
  }

  // ===== TRANSACTION REQUEST BUILDER =====

  public static TransactionRequestBuilder request() {
    return new TransactionRequestBuilder();
  }

  public static class TransactionRequestBuilder {
    private int transactionId = 1;
    private final List<Customer> customers = new ArrayList<>();

    public TransactionRequestBuilder withTransactionId(int id) {
      this.transactionId = id;
      return this;
    }

    public TransactionRequestBuilder withCustomer(Customer customer) {
      this.customers.add(customer);
      return this;
    }

    public TransactionRequestBuilder addChild(int age) {
      return withCustomer(customer().withName("Child").withAge(age).build());
    }

    public TransactionRequestBuilder addTeen(int age) {
      return withCustomer(customer().withName("Teen").withAge(age).build());
    }

    public TransactionRequestBuilder addAdult(int age) {
      return withCustomer(customer().withName("Adult").withAge(age).build());
    }

    public TransactionRequestBuilder addSenior(int age) {
      return withCustomer(customer().withName("Senior").withAge(age).build());
    }

    public TransactionRequest build() {
      return new TransactionRequest(transactionId, List.copyOf(customers));
    }
  }

  // ===== ASSERTION HELPERS =====

  public static void assertTotalCost(TransactionResponse response, BigDecimal expected) {
    Assertions.assertEquals(
        expected, response.totalCost(), "Total cost should match expected value");
  }

  public static void assertTicketSummary(
      TransactionResponse response, String type, int quantity, BigDecimal totalCost) {
    TicketSummary summary =
        response.tickets().stream()
            .filter(t -> type.equals(t.ticketType()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Ticket type '" + type + "' not found"));
    Assertions.assertEquals(quantity, summary.quantity(), "Quantity for " + type + " should match");
    Assertions.assertEquals(
        totalCost, summary.totalCost(), "Total cost for " + type + " should match");
  }

  public static void assertTicketTypes(TransactionResponse response, String... expectedTypes) {
    List<String> actualTypes = response.tickets().stream().map(TicketSummary::ticketType).toList();
    Assertions.assertEquals(
        List.of(expectedTypes), actualTypes, "Ticket types should match expected order");
  }
}
