package au.com.sportsbet.movietickets.config;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Immutable, constructor-bound age configuration. Spring Boot binds properties into this record via
 * the canonical constructor.
 */
@ConfigurationProperties(prefix = "ticket.age")
@Validated
public record AgeConfiguration(
    @Min(0) int childrenMax,
    @Min(0) int teenMin,
    @Min(0) int teenMax,
    @Min(0) int adultMin,
    @Min(0) int adultMax,
    @Min(0) int seniorMin) {

  @AssertTrue(
      message =
          "Age ranges must be consistent: childrenMax < teenMin <= teenMax < adultMin <= adultMax < seniorMin")
  public boolean isValidAgeRanges() {
    return childrenMax < teenMin
        && teenMin <= teenMax
        && teenMax < adultMin
        && adultMin <= adultMax
        && adultMax < seniorMin;
  }
}
