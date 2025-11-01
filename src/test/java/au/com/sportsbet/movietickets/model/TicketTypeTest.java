package au.com.sportsbet.movietickets.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TicketTypeTest {

    @Test
    void testFromAge_Children() {
        assertEquals(TicketType.CHILDREN, TicketType.fromAge(5));
        assertEquals(TicketType.CHILDREN, TicketType.fromAge(10));
        assertEquals(TicketType.CHILDREN, TicketType.fromAge(0));
    }

    @Test
    void testFromAge_Teen() {
        assertEquals(TicketType.TEEN, TicketType.fromAge(11));
        assertEquals(TicketType.TEEN, TicketType.fromAge(17));
    }

    @Test
    void testFromAge_Adult() {
        assertEquals(TicketType.ADULT, TicketType.fromAge(18));
        assertEquals(TicketType.ADULT, TicketType.fromAge(35));
        assertEquals(TicketType.ADULT, TicketType.fromAge(64));
    }

    @Test
    void testFromAge_Senior() {
        assertEquals(TicketType.SENIOR, TicketType.fromAge(65));
        assertEquals(TicketType.SENIOR, TicketType.fromAge(80));
        assertEquals(TicketType.SENIOR, TicketType.fromAge(100));
    }
}