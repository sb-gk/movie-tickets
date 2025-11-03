package au.com.sportsbet.movietickets.config;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "ticket.price")
@Validated
public class PricingConfiguration {

  @NotNull
  @DecimalMin(value = "0.0", inclusive = false)
  private BigDecimal adultPrice;

  @NotNull
  @DecimalMin(value = "0.0", inclusive = false)
  private BigDecimal teenPrice;

  @NotNull
  @DecimalMin(value = "0.0", inclusive = false)
  private BigDecimal childrenPrice;

  @NotNull
  @DecimalMin(value = "0.0")
  @DecimalMax(value = "1.0", inclusive = false)
  private BigDecimal seniorDiscountRate;

  @NotNull
  @DecimalMin(value = "0.0")
  @DecimalMax(value = "1.0", inclusive = false)
  private BigDecimal childrenGroupDiscountRate;

  @Min(0)
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
