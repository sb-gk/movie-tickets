package au.com.sportsbet.movietickets.model;

import static org.junit.jupiter.api.Assertions.*;

import au.com.sportsbet.movietickets.config.AgeConfiguration;
import org.junit.jupiter.api.Test;

class TicketTypeTest {

  private final AgeConfiguration config = createDefaultConfig();

  private AgeConfiguration createDefaultConfig() {
    return new AgeConfiguration(
        10, // childrenMax
        11, // teenMin
        17, // teenMax
        18, // adultMin
        64, // adultMax
        65 // seniorMin
        );
  }

  @Test
  void testFromAge_Children() {
    assertEquals(TicketType.CHILDREN, TicketType.fromAge(5, config));
    assertEquals(TicketType.CHILDREN, TicketType.fromAge(10, config));
    assertEquals(TicketType.CHILDREN, TicketType.fromAge(0, config));
  }

  @Test
  void testFromAge_Teen() {
    assertEquals(TicketType.TEEN, TicketType.fromAge(11, config));
    assertEquals(TicketType.TEEN, TicketType.fromAge(17, config));
  }

  @Test
  void testFromAge_Adult() {
    assertEquals(TicketType.ADULT, TicketType.fromAge(18, config));
    assertEquals(TicketType.ADULT, TicketType.fromAge(35, config));
    assertEquals(TicketType.ADULT, TicketType.fromAge(64, config));
  }

  @Test
  void testFromAge_Senior() {
    assertEquals(TicketType.SENIOR, TicketType.fromAge(65, config));
    assertEquals(TicketType.SENIOR, TicketType.fromAge(80, config));
    assertEquals(TicketType.SENIOR, TicketType.fromAge(100, config));
  }
}
