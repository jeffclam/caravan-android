package Routes;

import com.mapbox.services.api.directions.v5.models.StepIntersection;

import java.util.List;

/**
 * Created by Jeffrey on 5/18/2017.
 */

public class Intersection {
    private List<Double> location;
    private List<Integer> bearings;
    private List<Boolean> entry;
    private int in;
    private int out;
    //private IntersectionLanes[] lanes;


    public Intersection() {
    }

    public List<Double> getLocation() {
        return location;
    }

    public void setLocation(List<Double> location) {
        this.location = location;
    }

    public List<Integer> getBearings() {
        return bearings;
    }

    public void setBearings(List<Integer> bearings) {
        this.bearings = bearings;
    }

    public List<Boolean> getEntry() {
        return entry;
    }

    public void setEntry(List<Boolean> entry) {
        this.entry = entry;
    }

    public int getIn() {
        return in;
    }

    public void setIn(int in) {
        this.in = in;
    }

    public int getOut() {
        return out;
    }

    public void setOut(int out) {
        this.out = out;
    }

    public StepIntersection intersectionToStepIntersection() {
        StepIntersection intersection = new StepIntersection();

        double[] l = new double[location.size()];
        for (int i = 0; i < location.size(); i++)
            l[i] = location.get(i);
        intersection.setLocation(l);

        int[] b = new int[bearings.size()];
        for (int i = 0; i < bearings.size(); i++)
            b[i] = bearings.get(i);
        intersection.setBearings(b);

        boolean[] e = new boolean[entry.size()];
        for (int i = 0; i < entry.size(); i++)
            e[i] = entry.get(i);
        intersection.setEntry(e);

        intersection.setIn(in);
        intersection.setOut(out);

        return intersection;
    }
}
