package au.com.sportsbet.movietickets.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Summary of tickets for a specific type")
public record TicketSummary(
    @Schema(description = "Type of ticket", example = "Adult") String ticketType,
    @Schema(description = "Number of tickets of this type", example = "1") int quantity,
    @Schema(
            type = "string",
            format = "decimal",
            description =
                "Total cost for this ticket type, the monetary amount - should maintain exactly two decimal places.",
            example = "25.00",
            pattern = "^\\d+(\\.\\d{2})?$")
        BigDecimal totalCost) {}
