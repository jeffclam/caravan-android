package Routes;

import com.mapbox.services.api.directions.v5.models.IntersectionLanes;
import com.mapbox.services.api.directions.v5.models.LegStep;
import com.mapbox.services.api.directions.v5.models.StepIntersection;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jeffrey on 5/18/2017.
 */

public class Step {
    private double distance;
    private double duration;
    private String geometry;
    private String name;
    //private String ref;
    //private String destinations;
    private String mode;
    //private String pronunciation;
    //@SerializedName("rotary_name")
    //private String rotaryName;
    //@SerializedName("rotary_pronunciation")
    //private String rotaryPronunciation;
    private Maneuver maneuver;
    //private double weight;
    private Intersection intersections;

    public Step() {
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public Maneuver getManeuver() {
        return maneuver;
    }

    public void setManeuver(Maneuver maneuver) {
        this.maneuver = maneuver;
    }

    public Intersection getIntersections() {
        return intersections;
    }

    public void setIntersections(Intersection intersections) {
        this.intersections = intersections;
    }

    public LegStep stepToLegStep() {
        LegStep legStep = new LegStep();
        legStep.setDuration(this.getDuration());
        legStep.setDistance(this.getDistance());
        legStep.setGeometry(this.getGeometry());
        legStep.setManeuver(maneuver.maneuverToStepManeuver());
        List<StepIntersection> stepIntersections = new ArrayList<StepIntersection>();
        stepIntersections.add(intersections.intersectionToStepIntersection());
        legStep.setIntersections(stepIntersections);
        return legStep;
    }
}
