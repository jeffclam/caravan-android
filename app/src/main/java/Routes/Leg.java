package Routes;

import com.mapbox.services.api.directions.v5.models.LegStep;
import com.mapbox.services.api.directions.v5.models.RouteLeg;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jeffrey on 5/18/2017.
 */

public class Leg {
    private String description;
    private double distance;
    private String name;
    private String profileIdentifier;
    //private double duration;
    //private String summary;
    private List<Step> steps;
    //private LegAnnotation annotation;

    public Leg() {
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileIdentifier() {
        return profileIdentifier;
    }

    public void setProfileIdentifier(String profileIdentifier) {
        this.profileIdentifier = profileIdentifier;
    }

    public List<Step> getSteps() {
        return steps;
    }

    public void setSteps(List<Step> steps) {
        this.steps = steps;
    }

    public RouteLeg legToRouteLeg() {
        RouteLeg routeLeg = new RouteLeg();
        routeLeg.setDistance(this.getDistance());
        List<LegStep> legSteps = new ArrayList<LegStep>();
        for (Step s : this.getSteps()) {
            legSteps.add(s.stepToLegStep());
        }
        routeLeg.setSteps(legSteps);
        return routeLeg;
    }
}
