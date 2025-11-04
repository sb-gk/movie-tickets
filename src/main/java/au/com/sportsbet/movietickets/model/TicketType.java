package au.com.sportsbet.movietickets.model;

import au.com.sportsbet.movietickets.config.AgeConfiguration;

public enum TicketType {
  ADULT,
  SENIOR,
  TEEN,
  CHILDREN;

  /**
   * Determines the ticket type based on customer age and age configuration.
   *
   * @param age the customer's age in years
   * @param cfg the age configuration defining age ranges for each ticket type
   * @return the appropriate TicketType for the given age
   * @throws IllegalArgumentException if age is negative
   * @throws IllegalStateException if age configuration leaves gaps between ranges
   */
  public static TicketType fromAge(int age, AgeConfiguration cfg) {
    if (!cfg.isValidAgeRanges()) throw new IllegalStateException("Invalid age configuration");
    if (age < 0) throw new IllegalArgumentException("Age must be >= 0");
    if (age <= cfg.childrenMax()) return CHILDREN;
    if (age >= cfg.teenMin() && age <= cfg.teenMax()) return TEEN;
    if (age >= cfg.adultMin() && age <= cfg.adultMax()) return ADULT;
    if (age >= cfg.seniorMin()) return SENIOR;
    throw new IllegalStateException("Age configuration leaves a gap");
  }

  /**
   * Returns the display name for this ticket type in proper case format.
   *
   * @return the display name (e.g., "Adult", "Children", "Senior", "Teen")
   */
  public String getDisplayName() {
    return switch (this) {
      case ADULT -> "Adult";
      case SENIOR -> "Senior";
      case TEEN -> "Teen";
      case CHILDREN -> "Children";
    };
  }
}
