package au.com.sportsbet.movietickets.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "ticket.age")
public class AgeConfiguration {

    private int childrenMax;
    private int teenMin;
    private int teenMax;
    private int adultMin;
    private int adultMax;
    private int seniorMin;

    // Getters and setters
    public int getChildrenMax() { return childrenMax; }
    public void setChildrenMax(int childrenMax) { this.childrenMax = childrenMax; }

    public int getTeenMin() { return teenMin; }
    public void setTeenMin(int teenMin) { this.teenMin = teenMin; }

    public int getTeenMax() { return teenMax; }
    public void setTeenMax(int teenMax) { this.teenMax = teenMax; }

    public int getAdultMin() { return adultMin; }
    public void setAdultMin(int adultMin) { this.adultMin = adultMin; }

    public int getAdultMax() { return adultMax; }
    public void setAdultMax(int adultMax) { this.adultMax = adultMax; }

    public int getSeniorMin() { return seniorMin; }
    public void setSeniorMin(int seniorMin) { this.seniorMin = seniorMin; }
}