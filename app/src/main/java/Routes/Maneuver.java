package Routes;

import com.google.gson.annotations.SerializedName;
import com.mapbox.services.api.directions.v5.models.StepManeuver;

import java.util.List;

/**
 * Created by Jeffrey on 5/18/2017.
 */

public class Maneuver {
    private List<Double> location;
    @SerializedName("bearing_before")
    private double bearingBefore;
    @SerializedName("bearing_after")
    private double bearingAfter;
    private String type;
    private String modifier;
    private String instruction;
    //private Integer exit;

    public Maneuver() {
    }

    public List<Double> getLocation() {
        return location;
    }

    public void setLocation(List<Double> location) {
        this.location = location;
    }

    public double getBearingBefore() {
        return bearingBefore;
    }

    public void setBearingBefore(double bearingBefore) {
        this.bearingBefore = bearingBefore;
    }

    public double getBearingAfter() {
        return bearingAfter;
    }

    public void setBearingAfter(double bearingAfter) {
        this.bearingAfter = bearingAfter;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public StepManeuver maneuverToStepManeuver() {
        StepManeuver maneuver = new StepManeuver();

        double[] l = new double[location.size()];
        for (int i = 0; i < location.size(); i++)
            l[i] = location.get(i);
        maneuver.setLocation(l);

        maneuver.setBearingAfter(getBearingAfter());
        maneuver.setBearingBefore(getBearingBefore());
        maneuver.setInstruction(getInstruction());
        maneuver.setModifier(getModifier());
        maneuver.setType(getType());

        return maneuver;
    }
}