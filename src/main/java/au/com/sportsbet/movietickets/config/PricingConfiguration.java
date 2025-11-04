package au.com.sportsbet.movietickets.config;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Immutable, constructor-bound pricing configuration. Spring Boot will bind properties to this
 * record via constructor.
 */
@ConfigurationProperties(prefix = "ticket.price")
@Validated
public record PricingConfiguration(
    @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal adultPrice,
    @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal teenPrice,
    @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal childrenPrice,
    @NotNull
        @DecimalMin(value = "0.0", inclusive = true)
        @DecimalMax(value = "1.0", inclusive = false)
        BigDecimal seniorDiscountRate,
    @NotNull
        @DecimalMin(value = "0.0", inclusive = true)
        @DecimalMax(value = "1.0", inclusive = false)
        BigDecimal childrenGroupDiscountRate,
    @Min(0) int childrenGroupThreshold) {}
