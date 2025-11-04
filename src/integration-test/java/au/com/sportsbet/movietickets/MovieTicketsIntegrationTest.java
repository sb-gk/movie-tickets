package au.com.sportsbet.movietickets;

import static au.com.sportsbet.movietickets.util.TestBuilders.assertTicketSummary;
import static au.com.sportsbet.movietickets.util.TestBuilders.assertTotalCost;
import static au.com.sportsbet.movietickets.util.TestBuilders.request;
import static org.assertj.core.api.Assertions.assertThat;

import au.com.sportsbet.movietickets.model.*;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration tests for the complete Movie Tickets application. These tests start the full Spring
 * Boot application context and test the API endpoint end-to-end via HTTP.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = MovieTicketsApplication.class)
@ActiveProfiles("test")
class MovieTicketsIntegrationTest {

  @Autowired private TestRestTemplate restTemplate;

  private static final String CALCULATE_ENDPOINT = "/api/tickets/calculate";

  @Test
  void fullWorkflow_PDFSample1_ShouldCalculateCorrectly() {
    // Given: First sample from PDF - 1 Senior (70), 2 Children (5, 6)
    TransactionRequest request =
        request()
            .withTransactionId(1)
            .addSenior(70) // Senior
            .addChild(5) // Children
            .addChild(6) // Children
            .build();

    // When: POST request to calculate endpoint
    ResponseEntity<TransactionResponse> response =
        restTemplate.postForEntity(CALCULATE_ENDPOINT, request, TransactionResponse.class);

    // Then: Verify response
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();

    TransactionResponse body = response.getBody();
    assertThat(body.transactionId()).isEqualTo(1);
    assertThat(body.tickets()).hasSize(2);
    assertTotalCost(body, new BigDecimal("27.50"));

    // Verify Children tickets (no discount for only 2)
    assertTicketSummary(body, "Children", 2, new BigDecimal("10.00"));

    // Verify Senior ticket
    assertTicketSummary(body, "Senior", 1, new BigDecimal("17.50"));
  }

  @Test
  void fullWorkflow_PDFSample2_WithChildrenDiscount() {
    // Given: Second sample from PDF - 1 Adult, 3 Children (triggers discount), 1 Teen
    TransactionRequest request =
        request()
            .withTransactionId(2)
            .addAdult(36) // Adult
            .addChild(3) // Children
            .addChild(8) // Children
            .addChild(9) // Children
            .addTeen(17) // Teen
            .build();

    // When
    ResponseEntity<TransactionResponse> response =
        restTemplate.postForEntity(CALCULATE_ENDPOINT, request, TransactionResponse.class);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();

    TransactionResponse body = response.getBody();
    assertThat(body.transactionId()).isEqualTo(2);
    assertThat(body.tickets()).hasSize(3);
    assertTotalCost(body, new BigDecimal("48.25"));

    // Verify Adult ticket
    assertTicketSummary(body, "Adult", 1, new BigDecimal("25.00"));

    // Verify Children tickets (with 25% discount applied)
    assertTicketSummary(body, "Children", 3, new BigDecimal("11.25")); // 3 * 3.75

    // Verify Teen ticket
    assertTicketSummary(body, "Teen", 1, new BigDecimal("12.00"));
  }

  @Test
  void fullWorkflow_PDFSample3_AllTicketTypes() {
    // Given: Third sample from PDF - 1 of each ticket type
    TransactionRequest request =
        request()
            .withTransactionId(3)
            .addAdult(36) // Adult
            .addSenior(95) // Senior
            .addTeen(15) // Teen
            .addChild(10) // Children
            .build();

    // When
    ResponseEntity<TransactionResponse> response =
        restTemplate.postForEntity(CALCULATE_ENDPOINT, request, TransactionResponse.class);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();

    TransactionResponse body = response.getBody();
    assertThat(body.transactionId()).isEqualTo(3);
    assertThat(body.tickets()).hasSize(4);
    assertTotalCost(body, new BigDecimal("59.50"));

    // Verify all ticket types present
    List<String> ticketTypes = body.tickets().stream().map(TicketSummary::ticketType).toList();
    assertThat(ticketTypes).containsExactlyInAnyOrder("Adult", "Children", "Senior", "Teen");
  }

  @Test
  void fullWorkflow_AlphabeticalOrdering() {
    // Given: Customers in random order
    TransactionRequest request =
        request()
            .withTransactionId(4)
            .addTeen(15) // Teen
            .addSenior(70) // Senior
            .addChild(5) // Children
            .addAdult(30) // Adult
            .build();

    // When
    ResponseEntity<TransactionResponse> response =
        restTemplate.postForEntity(CALCULATE_ENDPOINT, request, TransactionResponse.class);

    // Then: Verify alphabetical order (Adult, Children, Senior, Teen)
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();

    List<String> ticketTypes =
        response.getBody().tickets().stream().map(TicketSummary::ticketType).toList();
    assertThat(ticketTypes).containsExactly("Adult", "Children", "Senior", "Teen");
  }

  @Test
  void validation_EmptyCustomerList_ShouldReturn400() {
    // Given: Request with empty customer list
    TransactionRequest request = request().withTransactionId(1).build();

    // When
    ResponseEntity<ErrorResponse> response =
        restTemplate.postForEntity(CALCULATE_ENDPOINT, request, ErrorResponse.class);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().status()).isEqualTo(400);
    assertThat(response.getBody().error()).isEqualTo("Validation Failed");
  }

  @Test
  void validation_NegativeTransactionId_ShouldReturn400() {
    // Given: Request with negative transaction ID
    TransactionRequest request = request().withTransactionId(-1).addAdult(30).build();

    // When
    ResponseEntity<ErrorResponse> response =
        restTemplate.postForEntity(CALCULATE_ENDPOINT, request, ErrorResponse.class);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().error()).isEqualTo("Validation Failed");
    assertThat(response.getBody().details())
        .anyMatch(detail -> detail.contains("Transaction ID must be positive"));
  }

  @Test
  void validation_BlankCustomerName_ShouldReturn400() {
    // Given: Request with blank customer name
    TransactionRequest request =
        request().withTransactionId(1).withCustomer(new Customer("", 30)).build();

    // When
    ResponseEntity<ErrorResponse> response =
        restTemplate.postForEntity(CALCULATE_ENDPOINT, request, ErrorResponse.class);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().details())
        .anyMatch(detail -> detail.contains("Customer name cannot be blank"));
  }

  @Test
  void validation_NegativeAge_ShouldReturn400() {
    // Given: Request with negative age
    TransactionRequest request =
        request().withTransactionId(1).withCustomer(new Customer("John Doe", -5)).build();

    // When
    ResponseEntity<ErrorResponse> response =
        restTemplate.postForEntity(CALCULATE_ENDPOINT, request, ErrorResponse.class);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().details())
        .anyMatch(detail -> detail.contains("Age must be at least 0"));
  }

  @Test
  void validation_AgeOver150_ShouldReturn400() {
    // Given: Request with age over maximum
    TransactionRequest request =
        request().withTransactionId(1).withCustomer(new Customer("Ancient Person", 200)).build();

    // When
    ResponseEntity<ErrorResponse> response =
        restTemplate.postForEntity(CALCULATE_ENDPOINT, request, ErrorResponse.class);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().details())
        .anyMatch(detail -> detail.contains("Age must be at most 150"));
  }

  @Test
  void largeTransaction_MultipleCustomersSameType() {
    // Given: Many customers of same type
    var builder = request().withTransactionId(6);
    for (int i = 0; i < 10; i++) {
      builder.addAdult(30);
    }
    TransactionRequest request = builder.build();

    // When
    ResponseEntity<TransactionResponse> response =
        restTemplate.postForEntity(CALCULATE_ENDPOINT, request, TransactionResponse.class);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();

    TransactionResponse body = response.getBody();
    assertThat(body.tickets()).hasSize(1);
    assertThat(body.tickets().get(0).ticketType()).isEqualTo("Adult");
    assertThat(body.tickets().get(0).quantity()).isEqualTo(10);
    assertThat(body.tickets().get(0).totalCost()).isEqualByComparingTo(new BigDecimal("250.00"));
  }

  @Test
  void childrenGroupDiscount_Exactly3Children_ShouldApplyDiscount() {
    // Given: Exactly 3 children (threshold)
    TransactionRequest request =
        request().withTransactionId(7).addChild(5).addChild(6).addChild(7).build();

    // When
    ResponseEntity<TransactionResponse> response =
        restTemplate.postForEntity(CALCULATE_ENDPOINT, request, TransactionResponse.class);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();

    TicketSummary childrenTicket = response.getBody().tickets().get(0);
    assertThat(childrenTicket.quantity()).isEqualTo(3);
    // 3 * (5.00 * 0.75) = 3 * 3.75 = 11.25
    assertThat(childrenTicket.totalCost()).isEqualByComparingTo(new BigDecimal("11.25"));
  }

  @Test
  void childrenGroupDiscount_2Children_ShouldNotApplyDiscount() {
    // Given: Only 2 children (below threshold)
    TransactionRequest request = request().withTransactionId(8).addChild(5).addChild(6).build();

    // When
    ResponseEntity<TransactionResponse> response =
        restTemplate.postForEntity(CALCULATE_ENDPOINT, request, TransactionResponse.class);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();

    TicketSummary childrenTicket = response.getBody().tickets().get(0);
    assertThat(childrenTicket.quantity()).isEqualTo(2);
    // 2 * 5.00 = 10.00 (no discount)
    assertThat(childrenTicket.totalCost()).isEqualByComparingTo(new BigDecimal("10.00"));
  }

  @Test
  void seniorPricing_ShouldBe30PercentOffAdult() {
    // Given: One senior customer
    TransactionRequest request = request().withTransactionId(9).addSenior(70).build();

    // When
    ResponseEntity<TransactionResponse> response =
        restTemplate.postForEntity(CALCULATE_ENDPOINT, request, TransactionResponse.class);

    // Then: Senior price should be 25.00 * 0.70 = 17.50
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();

    TicketSummary seniorTicket = response.getBody().tickets().get(0);
    assertThat(seniorTicket.ticketType()).isEqualTo("Senior");
    assertThat(seniorTicket.totalCost()).isEqualByComparingTo(new BigDecimal("17.50"));
  }
}
