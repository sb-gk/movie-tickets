package au.com.sportsbet.movietickets.config;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PricingConfigurationTest {

  @Autowired private PricingConfiguration pricingConfig;

  @Test
  void testPricingConfigurationLoaded() {
    assertNotNull(pricingConfig);
    assertNotNull(pricingConfig.getAdultPrice());
    assertNotNull(pricingConfig.getTeenPrice());
    assertNotNull(pricingConfig.getChildrenPrice());
    assertNotNull(pricingConfig.getSeniorDiscountRate());
    assertNotNull(pricingConfig.getChildrenGroupDiscountRate());
    assertTrue(pricingConfig.getChildrenGroupThreshold() > 0);
  }

  @Test
  void testAllPricesArePositive() {
    assertTrue(
        pricingConfig.getAdultPrice().compareTo(BigDecimal.ZERO) > 0,
        "Adult price should be positive");
    assertTrue(
        pricingConfig.getTeenPrice().compareTo(BigDecimal.ZERO) > 0,
        "Teen price should be positive");
    assertTrue(
        pricingConfig.getChildrenPrice().compareTo(BigDecimal.ZERO) > 0,
        "Children price should be positive");
  }

  @Test
  void testDiscountRatesAreValid() {
    // Discount rates should be between 0 and 1 (0% to 100%)
    assertTrue(
        pricingConfig.getSeniorDiscountRate().compareTo(BigDecimal.ZERO) >= 0
            && pricingConfig.getSeniorDiscountRate().compareTo(BigDecimal.ONE) <= 1,
        "Senior discount rate should be between 0 and 1");
    assertTrue(
        pricingConfig.getChildrenGroupDiscountRate().compareTo(BigDecimal.ZERO) >= 0
            && pricingConfig.getChildrenGroupDiscountRate().compareTo(BigDecimal.ONE) <= 1,
        "Children group discount rate should be between 0 and 1");
  }

  @Test
  void testChildrenGroupThresholdIsReasonable() {
    assertTrue(
        pricingConfig.getChildrenGroupThreshold() >= 2
            && pricingConfig.getChildrenGroupThreshold() <= 10,
        "Children group threshold should be between 2 and 10");
  }

  @Test
  void testPricingLogic() {
    // Children should be cheapest
    assertTrue(
        pricingConfig.getChildrenPrice().compareTo(pricingConfig.getTeenPrice()) < 0,
        "Children price should be less than teen price");
    assertTrue(
        pricingConfig.getChildrenPrice().compareTo(pricingConfig.getAdultPrice()) < 0,
        "Children price should be less than adult price");

    // Teen should be cheaper than adult
    assertTrue(
        pricingConfig.getTeenPrice().compareTo(pricingConfig.getAdultPrice()) < 0,
        "Teen price should be less than adult price");

    // Senior price (after discount) should be less than adult price
    BigDecimal seniorPrice =
        pricingConfig
            .getAdultPrice()
            .multiply(BigDecimal.ONE.subtract(pricingConfig.getSeniorDiscountRate()));
    assertTrue(
        seniorPrice.compareTo(pricingConfig.getAdultPrice()) < 0,
        "Senior price should be less than adult price");
  }
}
