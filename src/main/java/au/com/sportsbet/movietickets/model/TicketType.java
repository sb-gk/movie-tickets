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
    if (age < 0) throw new IllegalArgumentException("Age must be >= 0");
    if (age <= cfg.getChildrenMax()) return CHILDREN;
    if (age >= cfg.getTeenMin() && age <= cfg.getTeenMax()) return TEEN;
    if (age >= cfg.getAdultMin() && age <= cfg.getAdultMax()) return ADULT;
    if (age >= cfg.getSeniorMin()) return SENIOR;
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
