package au.com.sportsbet.movietickets.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class PricingConfigurationValidationFailureTest {

  // Proves that @Validated constraints on PricingConfiguration are enforced at bind/startup time.
  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner()
          .withConfiguration(
              AutoConfigurations.of(
                  ConfigurationPropertiesAutoConfiguration.class,
                  ValidationAutoConfiguration.class))
          .withUserConfiguration(PricingConfiguration.class);

  @Test
  void discountRateEqualToOneFailsValidation() {
    contextRunner
        .withPropertyValues(
            "ticket.price.adult-price=25.00",
            "ticket.price.teen-price=12.00",
            "ticket.price.children-price=5.00",
            // Invalid per @DecimalMax(value = \"1.0\", inclusive = false)
            "ticket.price.senior-discount-rate=1.0",
            "ticket.price.children-group-discount-rate=0.25",
            "ticket.price.children-group-threshold=3")
        .run(context -> assertThat(context).hasFailed());
  }

  @Test
  void negativeGroupDiscountFailsValidation() {
    contextRunner
        .withPropertyValues(
            "ticket.price.adult-price=25.00",
            "ticket.price.teen-price=12.00",
            "ticket.price.children-price=5.00",
            "ticket.price.senior-discount-rate=0.30",
            // Invalid per @DecimalMin(value = \"0.0\", inclusive = true)
            "ticket.price.children-group-discount-rate=-0.10",
            "ticket.price.children-group-threshold=3")
        .run(context -> assertThat(context).hasFailed());
  }

  @Test
  void zeroAdultPriceFailsValidation() {
    contextRunner
        .withPropertyValues(
            // Invalid per @DecimalMin(value = \"0.0\", inclusive = false)
            "ticket.price.adult-price=0.00",
            "ticket.price.teen-price=12.00",
            "ticket.price.children-price=5.00",
            "ticket.price.senior-discount-rate=0.30",
            "ticket.price.children-group-discount-rate=0.25",
            "ticket.price.children-group-threshold=3")
        .run(context -> assertThat(context).hasFailed());
  }
}
