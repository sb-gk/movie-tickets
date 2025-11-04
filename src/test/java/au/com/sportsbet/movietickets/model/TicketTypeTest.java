package au.com.sportsbet.movietickets.model;

import static au.com.sportsbet.movietickets.util.TestBuilders.ageConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import au.com.sportsbet.movietickets.config.AgeConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TicketTypeTest {

  private final AgeConfiguration defaultConfig = ageConfig();

  @ParameterizedTest
  @CsvSource({
    "0, CHILDREN",
    "10, CHILDREN",
    "11, TEEN",
    "17, TEEN",
    "18, ADULT",
    "64, ADULT",
    "65, SENIOR",
    "100, SENIOR"
  })
  void testFromAge_MapsCorrectly(int age, TicketType expectedType) {
    assertEquals(expectedType, TicketType.fromAge(age, defaultConfig));
  }

  @Test
  void testFromAge_NegativeAge_ThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> TicketType.fromAge(-1, defaultConfig));
  }

  @Test
  void testFromAge_InvalidConfig_ThrowsException() {
    // Invalid config: teen min should be > children max
    AgeConfiguration invalidConfig = new AgeConfiguration(12, 11, 17, 18, 64, 65);
    assertThrows(IllegalStateException.class, () -> TicketType.fromAge(15, invalidConfig));
  }
}
