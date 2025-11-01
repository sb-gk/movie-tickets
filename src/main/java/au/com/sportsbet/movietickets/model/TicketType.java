package au.com.sportsbet.movietickets.model;

public enum TicketType {
    ADULT,
    SENIOR,
    TEEN,
    CHILDREN;

    public static TicketType fromAge(int age) {
        if (age < 11) {
            return CHILDREN;
        } else if (age < 18) {
            return TEEN;
        } else if (age < 65) {
            return ADULT;
        } else {
            return SENIOR;
        }
    }
}