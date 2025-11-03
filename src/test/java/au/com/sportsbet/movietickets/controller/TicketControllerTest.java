package au.com.sportsbet.movietickets.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import au.com.sportsbet.movietickets.model.*;
import au.com.sportsbet.movietickets.service.TransactionProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TicketController.class)
@ContextConfiguration(classes = {TicketControllerTest.TestConfig.class})
class TicketControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private TransactionProcessor transactionProcessor;

  @TestConfiguration
  static class TestConfig {
    @Bean
    public TransactionProcessor transactionProcessor() {
      return Mockito.mock(TransactionProcessor.class);
    }
  }

  @Test
  void testCalculateTicketCost() throws Exception {
    // Given
    TransactionRequest request = new TransactionRequest(1, List.of(new Customer("John Doe", 30)));

    TransactionResponse expectedResponse =
        new TransactionResponse(
            1,
            List.of(new TicketSummary("ADULT", 1, new BigDecimal("25.00"))),
            new BigDecimal("25.00"));

    when(transactionProcessor.processTransaction(any(TransactionRequest.class)))
        .thenReturn(expectedResponse);

    // When & Then
    mockMvc
        .perform(
            post("/api/tickets/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.transactionId").value(1))
        .andExpect(jsonPath("$.tickets[0].ticketType").value("ADULT"))
        .andExpect(jsonPath("$.tickets[0].quantity").value(1))
        .andExpect(jsonPath("$.tickets[0].totalCost").value(25.00))
        .andExpect(jsonPath("$.totalCost").value(25.00));
  }

  @Test
  void testCalculateTicketCost_InvalidRequest() throws Exception {
    // Given - empty request (will fail validation)
    String invalidRequest = "{}";

    // When & Then - Validation should return 400 Bad Request
    mockMvc
        .perform(
            post("/api/tickets/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("Validation Failed"));
  }
}
