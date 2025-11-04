package au.com.sportsbet.movietickets;

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
        new TransactionRequest(
            1,
            List.of(
                new Customer("John Smith", 70), // Senior
                new Customer("Jane Doe", 5), // Children
                new Customer("Bob Doe", 6) // Children
                ));

    // When: POST request to calculate endpoint
    ResponseEntity<TransactionResponse> response =
        restTemplate.postForEntity(CALCULATE_ENDPOINT, request, TransactionResponse.class);

    // Then: Verify response
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();

    TransactionResponse body = response.getBody();
    assertThat(body.transactionId()).isEqualTo(1);
    assertThat(body.tickets()).hasSize(2);
    assertThat(body.totalCost()).isEqualByComparingTo(new BigDecimal("27.50"));

    // Verify Children tickets (no discount for only 2)
    TicketSummary childrenTicket =
        body.tickets().stream()
            .filter(t -> "Children".equals(t.ticketType()))
            .findFirst()
            .orElseThrow();
    assertThat(childrenTicket.quantity()).isEqualTo(2);
    assertThat(childrenTicket.totalCost()).isEqualByComparingTo(new BigDecimal("10.00"));

    // Verify Senior ticket
    TicketSummary seniorTicket =
        body.tickets().stream()
            .filter(t -> "Senior".equals(t.ticketType()))
            .findFirst()
            .orElseThrow();
    assertThat(seniorTicket.quantity()).isEqualTo(1);
    assertThat(seniorTicket.totalCost()).isEqualByComparingTo(new BigDecimal("17.50"));
  }

  @Test
  void fullWorkflow_PDFSample2_WithChildrenDiscount() {
    // Given: Second sample from PDF - 1 Adult, 3 Children (triggers discount), 1 Teen
    TransactionRequest request =
        new TransactionRequest(
            2,
            List.of(
                new Customer("Billy Kidd", 36), // Adult
                new Customer("Zoe Daniels", 3), // Children
                new Customer("George White", 8), // Children
                new Customer("Tommy Anderson", 9), // Children
                new Customer("Joe Smith", 17) // Teen
                ));

    // When
    ResponseEntity<TransactionResponse> response =
        restTemplate.postForEntity(CALCULATE_ENDPOINT, request, TransactionResponse.class);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();

    TransactionResponse body = response.getBody();
    assertThat(body.transactionId()).isEqualTo(2);
    assertThat(body.tickets()).hasSize(3);
    assertThat(body.totalCost()).isEqualByComparingTo(new BigDecimal("48.25"));

    // Verify Adult ticket
    TicketSummary adultTicket =
        body.tickets().stream()
            .filter(t -> "Adult".equals(t.ticketType()))
            .findFirst()
            .orElseThrow();
    assertThat(adultTicket.quantity()).isEqualTo(1);
    assertThat(adultTicket.totalCost()).isEqualByComparingTo(new BigDecimal("25.00"));

    // Verify Children tickets (with 25% discount applied)
    TicketSummary childrenTicket =
        body.tickets().stream()
            .filter(t -> "Children".equals(t.ticketType()))
            .findFirst()
            .orElseThrow();
    assertThat(childrenTicket.quantity()).isEqualTo(3);
    assertThat(childrenTicket.totalCost())
        .isEqualByComparingTo(new BigDecimal("11.25")); // 3 * 3.75

    // Verify Teen ticket
    TicketSummary teenTicket =
        body.tickets().stream()
            .filter(t -> "Teen".equals(t.ticketType()))
            .findFirst()
            .orElseThrow();
    assertThat(teenTicket.quantity()).isEqualTo(1);
    assertThat(teenTicket.totalCost()).isEqualByComparingTo(new BigDecimal("12.00"));
  }

  @Test
  void fullWorkflow_PDFSample3_AllTicketTypes() {
    // Given: Third sample from PDF - 1 of each ticket type
    TransactionRequest request =
        new TransactionRequest(
            3,
            List.of(
                new Customer("Jesse James", 36), // Adult
                new Customer("Daniel Anderson", 95), // Senior
                new Customer("Mary Jones", 15), // Teen
                new Customer("Michelle Parker", 10) // Children
                ));

    // When
    ResponseEntity<TransactionResponse> response =
        restTemplate.postForEntity(CALCULATE_ENDPOINT, request, TransactionResponse.class);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();

    TransactionResponse body = response.getBody();
    assertThat(body.transactionId()).isEqualTo(3);
    assertThat(body.tickets()).hasSize(4);
    assertThat(body.totalCost()).isEqualByComparingTo(new BigDecimal("59.50"));

    // Verify all ticket types present
    List<String> ticketTypes = body.tickets().stream().map(TicketSummary::ticketType).toList();
    assertThat(ticketTypes).containsExactlyInAnyOrder("Adult", "Children", "Senior", "Teen");
  }

  @Test
  void fullWorkflow_AlphabeticalOrdering() {
    // Given: Customers in random order
    TransactionRequest request =
        new TransactionRequest(
            4,
            List.of(
                new Customer("Teen", 15), // Teen
                new Customer("Senior", 70), // Senior
                new Customer("Child", 5), // Children
                new Customer("Adult", 30) // Adult
                ));

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
    TransactionRequest request = new TransactionRequest(1, List.of());

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
    TransactionRequest request = new TransactionRequest(-1, List.of(new Customer("John Doe", 30)));

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
    TransactionRequest request = new TransactionRequest(1, List.of(new Customer("", 30)));

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
    TransactionRequest request = new TransactionRequest(1, List.of(new Customer("John Doe", -5)));

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
        new TransactionRequest(1, List.of(new Customer("Ancient Person", 200)));

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
  void boundaryAges_ShouldMapToCorrectTicketTypes() {
    // Given: Customers at boundary ages
    TransactionRequest request =
        new TransactionRequest(
            5,
            List.of(
                new Customer("Boundary10", 10), // CHILDREN (max)
                new Customer("Boundary11", 11), // TEEN (min)
                new Customer("Boundary17", 17), // TEEN (max)
                new Customer("Boundary18", 18), // ADULT (min)
                new Customer("Boundary64", 64), // ADULT (max)
                new Customer("Boundary65", 65) // SENIOR (min)
                ));

    // When
    ResponseEntity<TransactionResponse> response =
        restTemplate.postForEntity(CALCULATE_ENDPOINT, request, TransactionResponse.class);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();

    TransactionResponse body = response.getBody();
    assertThat(body.tickets()).hasSize(4);

    // Verify quantities by checking each ticket type
    var ticketMap = new java.util.HashMap<String, Integer>();
    body.tickets().forEach(ticket -> ticketMap.put(ticket.ticketType(), ticket.quantity()));

    assertThat(ticketMap.get("Children")).isEqualTo(1); // age 10
    assertThat(ticketMap.get("Teen")).isEqualTo(2); // ages 11, 17
    assertThat(ticketMap.get("Adult")).isEqualTo(2); // ages 18, 64
    assertThat(ticketMap.get("Senior")).isEqualTo(1); // age 65
  }

  @Test
  void largeTransaction_MultipleCustomersSameType() {
    // Given: Many customers of same type
    var customers = new java.util.ArrayList<Customer>();
    for (int i = 0; i < 10; i++) {
      customers.add(new Customer("Adult" + i, 30));
    }
    TransactionRequest request = new TransactionRequest(6, customers);

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
        new TransactionRequest(
            7,
            List.of(
                new Customer("Child1", 5), new Customer("Child2", 6), new Customer("Child3", 7)));

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
    TransactionRequest request =
        new TransactionRequest(8, List.of(new Customer("Child1", 5), new Customer("Child2", 6)));

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
    TransactionRequest request =
        new TransactionRequest(9, List.of(new Customer("Senior Citizen", 70)));

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
