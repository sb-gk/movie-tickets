package au.com.sportsbet.movietickets.model;

import au.com.sportsbet.movietickets.config.AgeConfiguration;

public enum TicketType {
    ADULT,
    SENIOR,
    TEEN,
    CHILDREN;

    public static TicketType fromAge(int age, AgeConfiguration config) {
        if (age <= config.getChildrenMax()) {
            return CHILDREN;
        } else if (age >= config.getTeenMin() && age <= config.getTeenMax()) {
            return TEEN;
        } else if (age >= config.getAdultMin() && age <= config.getAdultMax()) {
            return ADULT;
        } else if (age >= config.getSeniorMin()) {
            return SENIOR;
        }
        throw new IllegalArgumentException("Invalid age: " + age);
    }

}