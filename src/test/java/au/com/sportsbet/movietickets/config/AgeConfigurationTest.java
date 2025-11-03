package au.com.sportsbet.movietickets.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AgeConfigurationTest {

  @Autowired private AgeConfiguration ageConfig;

  @Test
  void testAgeConfigurationLoaded() {
    assertNotNull(ageConfig);
    assertTrue(ageConfig.getChildrenMax() > 0);
    assertTrue(ageConfig.getTeenMin() > 0);
    assertTrue(ageConfig.getTeenMax() > 0);
    assertTrue(ageConfig.getAdultMin() > 0);
    assertTrue(ageConfig.getAdultMax() > 0);
    assertTrue(ageConfig.getSeniorMin() > 0);
  }

  @Test
  void testAgeRangesHaveNoGaps() {
    // Children range should end just before teen range starts
    assertEquals(
        ageConfig.getTeenMin() - 1,
        ageConfig.getChildrenMax(),
        "Children max age should be exactly 1 less than teen min age");

    // Teen range should end just before adult range starts
    assertEquals(
        ageConfig.getAdultMin() - 1,
        ageConfig.getTeenMax(),
        "Teen max age should be exactly 1 less than adult min age");

    // Adult range should end just before senior range starts
    assertEquals(
        ageConfig.getSeniorMin() - 1,
        ageConfig.getAdultMax(),
        "Adult max age should be exactly 1 less than senior min age");
  }

  @Test
  void testAgeRangesDoNotOverlap() {
    // Teen should start after children
    assertTrue(
        ageConfig.getTeenMin() > ageConfig.getChildrenMax(),
        "Teen min age should be greater than children max age");

    // Adult should start after teen
    assertTrue(
        ageConfig.getAdultMin() > ageConfig.getTeenMax(),
        "Adult min age should be greater than teen max age");

    // Senior should start after adult
    assertTrue(
        ageConfig.getSeniorMin() > ageConfig.getAdultMax(),
        "Senior min age should be greater than adult max age");
  }

  @Test
  void testRangesAreLogicallyOrdered() {
    assertTrue(ageConfig.getTeenMin() <= ageConfig.getTeenMax(), "Teen min should be <= teen max");
    assertTrue(
        ageConfig.getAdultMin() <= ageConfig.getAdultMax(), "Adult min should be <= adult max");
  }
}
