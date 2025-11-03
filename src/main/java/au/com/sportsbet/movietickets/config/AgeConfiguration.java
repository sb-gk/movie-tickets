package au.com.sportsbet.movietickets.config;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "ticket.age")
@Validated
public class AgeConfiguration {

  @Min(0)
  private int childrenMax;

  @Min(0)
  private int teenMin;

  @Min(0)
  private int teenMax;

  @Min(0)
  private int adultMin;

  @Min(0)
  private int adultMax;

  @Min(0)
  private int seniorMin;

  @AssertTrue(
      message =
          "Age ranges must be consistent: childrenMax < teenMin <= teenMax < adultMin <= adultMax < seniorMin")
  public boolean isValidAgeRanges() {
    return childrenMax < teenMin
        && teenMin <= teenMax
        && teenMax < adultMin
        && adultMin <= adultMax
        && adultMax < seniorMin;
  }

  // Getters and setters
  public int getChildrenMax() {
    return childrenMax;
  }

  public void setChildrenMax(int childrenMax) {
    this.childrenMax = childrenMax;
  }

  public int getTeenMin() {
    return teenMin;
  }

  public void setTeenMin(int teenMin) {
    this.teenMin = teenMin;
  }

  public int getTeenMax() {
    return teenMax;
  }

  public void setTeenMax(int teenMax) {
    this.teenMax = teenMax;
  }

  public int getAdultMin() {
    return adultMin;
  }

  public void setAdultMin(int adultMin) {
    this.adultMin = adultMin;
  }

  public int getAdultMax() {
    return adultMax;
  }

  public void setAdultMax(int adultMax) {
    this.adultMax = adultMax;
  }

  public int getSeniorMin() {
    return seniorMin;
  }

  public void setSeniorMin(int seniorMin) {
    this.seniorMin = seniorMin;
  }
}
