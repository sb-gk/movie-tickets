package au.com.sportsbet.movietickets.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Response containing calculated ticket costs for a transaction")
public record TransactionResponse(
    @Schema(description = "Transaction ID", example = "1") int transactionId,
    @Schema(description = "List of ticket summaries by type") List<TicketSummary> tickets,
    @Schema(
            type = "string",
            format = "decimal",
            description =
                "Total cost of all tickets, the monetary amount - should maintain exactly two decimal places.",
            example = "27.50",
            pattern = "^\\d+(\\.\\d{2})?$")
        BigDecimal totalCost) {}
