package au.com.sportsbet.movietickets.service;

import au.com.sportsbet.movietickets.config.PricingConfiguration;
import au.com.sportsbet.movietickets.model.TicketType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class TicketPriceCalculator {

    private final PricingConfiguration pricingConfig;

    public TicketPriceCalculator(PricingConfiguration pricingConfig) {
        this.pricingConfig = pricingConfig;
    }

    public BigDecimal calculateBasePrice(TicketType ticketType) {
        return switch (ticketType) {
            case ADULT -> pricingConfig.getAdult();
            case SENIOR -> {
                // Senior gets discount off adult price
                BigDecimal adultPrice = pricingConfig.getAdult();
                BigDecimal discount = adultPrice.multiply(pricingConfig.getSeniorDiscountRate());
                yield adultPrice.subtract(discount).setScale(2, java.math.RoundingMode.HALF_UP);
            }
            case TEEN -> pricingConfig.getTeen();
            case CHILDREN -> pricingConfig.getChildren();
        };
    }

    public BigDecimal calculatePriceWithDiscount(TicketType ticketType, int quantity, Map<TicketType, Integer> ticketCounts) {
        BigDecimal basePrice = calculateBasePrice(ticketType);

        // Apply group discount for children if threshold is met
        if (ticketType == TicketType.CHILDREN &&
            ticketCounts.getOrDefault(TicketType.CHILDREN, 0) >= pricingConfig.getChildrenGroupThreshold()) {
            BigDecimal discount = basePrice.multiply(pricingConfig.getChildrenGroupDiscountRate());
            basePrice = basePrice.subtract(discount).setScale(2, java.math.RoundingMode.HALF_UP);
        }

        return basePrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, java.math.RoundingMode.HALF_UP);
    }
}