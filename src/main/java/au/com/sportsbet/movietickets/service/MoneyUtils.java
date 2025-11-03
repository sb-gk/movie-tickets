package au.com.sportsbet.movietickets.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utility class for money-related operations, providing shared constants for scaling and rounding.
 */
public final class MoneyUtils {

  public static final int SCALE = 2;
  public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

  private MoneyUtils() {
    // Utility class, prevent instantiation
  }

  /**
   * Scales a BigDecimal to the standard money scale with the standard rounding mode.
   *
   * @param value the BigDecimal to scale
   * @return the scaled BigDecimal
   */
  public static BigDecimal scale(BigDecimal value) {
    return value.setScale(SCALE, ROUNDING_MODE);
  }
}
