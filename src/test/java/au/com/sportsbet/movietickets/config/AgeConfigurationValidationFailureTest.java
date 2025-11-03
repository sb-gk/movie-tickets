package au.com.sportsbet.movietickets.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class AgeConfigurationValidationFailureTest {

  // Proves that @Validated + @AssertTrue on AgeConfiguration is enforced at bind/startup time.
  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner()
          .withConfiguration(
              AutoConfigurations.of(
                  ConfigurationPropertiesAutoConfiguration.class,
                  ValidationAutoConfiguration.class))
          .withUserConfiguration(AgeConfiguration.class);

  @Test
  void invalidAgeOrderingFailsValidation() {
    contextRunner
        .withPropertyValues(
            // Invalid: childrenMax must be < teenMin (12 < 11 is false)
            "ticket.age.children-max=12",
            "ticket.age.teen-min=11",
            "ticket.age.teen-max=17",
            "ticket.age.adult-min=18",
            "ticket.age.adult-max=64",
            "ticket.age.senior-min=65")
        .run(context -> assertThat(context).hasFailed());
  }

  @Test
  void teenMaxNotLessThanAdultMinFailsValidation() {
    contextRunner
        .withPropertyValues(
            // Invalid: teenMax must be < adultMin (18 < 18 is false)
            "ticket.age.children-max=10",
            "ticket.age.teen-min=11",
            "ticket.age.teen-max=18",
            "ticket.age.adult-min=18",
            "ticket.age.adult-max=64",
            "ticket.age.senior-min=65")
        .run(context -> assertThat(context).hasFailed());
  }
}
