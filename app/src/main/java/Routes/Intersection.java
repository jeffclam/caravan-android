package Routes;

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
}
