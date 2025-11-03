package au.com.sportsbet.movietickets.config;

import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "ticket.price")
public class PricingConfiguration {

  private BigDecimal adultPrice;
  private BigDecimal teenPrice;
  private BigDecimal childrenPrice;
  private BigDecimal seniorDiscountRate;
  private BigDecimal childrenGroupDiscountRate;
  private int childrenGroupThreshold;

  // Getters and setters
  public BigDecimal getAdultPrice() {
    return adultPrice;
  }

  public void setAdultPrice(BigDecimal adultPrice) {
    this.adultPrice = adultPrice;
  }

  public BigDecimal getTeenPrice() {
    return teenPrice;
  }

  public void setTeenPrice(BigDecimal teenPrice) {
    this.teenPrice = teenPrice;
  }

  public BigDecimal getChildrenPrice() {
    return childrenPrice;
  }

  public void setChildrenPrice(BigDecimal childrenPrice) {
    this.childrenPrice = childrenPrice;
  }

  public BigDecimal getSeniorDiscountRate() {
    return seniorDiscountRate;
  }

  public void setSeniorDiscountRate(BigDecimal seniorDiscountRate) {
    this.seniorDiscountRate = seniorDiscountRate;
  }

  public BigDecimal getChildrenGroupDiscountRate() {
    return childrenGroupDiscountRate;
  }

  public void setChildrenGroupDiscountRate(BigDecimal childrenGroupDiscountRate) {
    this.childrenGroupDiscountRate = childrenGroupDiscountRate;
  }

  public int getChildrenGroupThreshold() {
    return childrenGroupThreshold;
  }

  public void setChildrenGroupThreshold(int childrenGroupThreshold) {
    this.childrenGroupThreshold = childrenGroupThreshold;
  }
}
