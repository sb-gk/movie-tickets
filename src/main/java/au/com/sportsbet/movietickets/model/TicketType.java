package au.com.sportsbet.movietickets.model;

import au.com.sportsbet.movietickets.config.AgeConfiguration;

public enum TicketType {
  ADULT,
  SENIOR,
  TEEN,
  CHILDREN;

  public static TicketType fromAge(int age, AgeConfiguration cfg) {
    if (age < 0) throw new IllegalArgumentException("Age must be >= 0");
    if (age <= cfg.getChildrenMax()) return CHILDREN;
    if (age >= cfg.getTeenMin() && age <= cfg.getTeenMax()) return TEEN;
    if (age >= cfg.getAdultMin() && age <= cfg.getAdultMax()) return ADULT;
    if (age >= cfg.getSeniorMin()) return SENIOR;
    throw new IllegalStateException("Age configuration leaves a gap");
  }
}
