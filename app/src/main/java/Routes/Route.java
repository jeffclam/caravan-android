package Routes;

import java.util.List;

/**
 * Created by Jeffrey on 5/18/2017.
 */

public class Route {
    private double distance;
    private double duration;
    private String geometry;
    // private double weight;
    // @SerializedName("weight_name")
    // private String weightName;
    private List<Leg> legs;
    private String profileIdentifier;

    public Route() {
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public String getGeometry() {
        return geometry;
    }

    public void setGeometry(String geometry) {
        this.geometry = geometry;
    }

    public List<Leg> getLegs() {
        return legs;
    }

    public void setLegs(List<Leg> legs) {
        this.legs = legs;
    }

    public String getProfileIdentifier() {
        return profileIdentifier;
    }

    public void setProfileIdentifier(String profileIdentifier) {
        this.profileIdentifier = profileIdentifier;
    }
}
