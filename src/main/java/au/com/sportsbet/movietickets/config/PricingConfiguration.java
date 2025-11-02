package au.com.sportsbet.movietickets.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@ConfigurationProperties(prefix = "ticket.price")
public class PricingConfiguration {

    private BigDecimal adult;
    private BigDecimal teen;
    private BigDecimal children;
    private BigDecimal seniorDiscountRate;
    private BigDecimal childrenGroupDiscountRate;
    private int childrenGroupThreshold;

    // Getters and setters
    public BigDecimal getAdult() { return adult; }
    public void setAdult(BigDecimal adult) { this.adult = adult; }

    public BigDecimal getTeen() { return teen; }
    public void setTeen(BigDecimal teen) { this.teen = teen; }

    public BigDecimal getChildren() { return children; }
    public void setChildren(BigDecimal children) { this.children = children; }

    public BigDecimal getSeniorDiscountRate() { return seniorDiscountRate; }
    public void setSeniorDiscountRate(BigDecimal seniorDiscountRate) { this.seniorDiscountRate = seniorDiscountRate; }

    public BigDecimal getChildrenGroupDiscountRate() { return childrenGroupDiscountRate; }
    public void setChildrenGroupDiscountRate(BigDecimal childrenGroupDiscountRate) { this.childrenGroupDiscountRate = childrenGroupDiscountRate; }

    public int getChildrenGroupThreshold() { return childrenGroupThreshold; }
    public void setChildrenGroupThreshold(int childrenGroupThreshold) { this.childrenGroupThreshold = childrenGroupThreshold; }
}