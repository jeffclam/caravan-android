package com.caravan.senior_project.caravan_android;

import com.mapbox.services.api.directions.v5.models.LegAnnotation;
import com.mapbox.services.api.directions.v5.models.LegStep;
import com.mapbox.services.api.directions.v5.models.RouteLeg;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rohin_000 on 5/17/2017.
 */

public class MyRouteLeg {
    private double distance;
    private double duration;
    private String summary;
    private List<MyLegStep> steps = new ArrayList<MyLegStep>();
    private LegAnnotation annotation;

    /**
     * Empty constructor
     *
     * @since 2.0.0
     */
    public MyRouteLeg() {
    }

    public MyRouteLeg(RouteLeg leg) {
        this.distance = leg.getDistance();
        this.duration = leg.getDuration();
        this.summary = leg.getSummary();
        for (int i = 0; i < leg.getSteps().size(); i++) {
            this.steps.add(new MyLegStep(leg.getSteps().get(i)));
        }
    }

    /**
     * The distance traveled from one waypoint to another.
     *
     * @return a double number with unit meters.
     * @since 1.0.0
     */
    public double getDistance() {
        return distance;
    }

    /**
     * The distance traveled from one waypoint to another.
     *
     * @param distance a double number with unit meters.
     * @since 2.1.0
     */
    public void setDistance(double distance) {
        this.distance = distance;
    }

    /**
     * The estimated travel time from one waypoint to another.
     *
     * @return a double number with unit seconds.
     * @since 1.0.0
     */
    public double getDuration() {
        return duration;
    }

    /**
     * The estimated travel time from one waypoint to another.
     *
     * @param duration a double number with unit seconds.
     * @since 2.1.0
     */
    public void setDuration(double duration) {
        this.duration = duration;
    }

    /**
     * A short human-readable summary of major roads traversed. Useful to distinguish alternatives.
     *
     * @return String with summary.
     * @since 1.0.0
     */
    public String getSummary() {
        return summary;
    }

    /**
     * A short human-readable summary of major roads traversed. Useful to distinguish alternatives.
     *
     * @param summary a String with a human-readable summary.
     * @since 2.1.0
     */
    public void setSummary(String summary) {
        this.summary = summary;
    }

    /**
     * Gives a List including all the steps to get from one waypoint to another.
     *
     * @return List of {@link LegStep}.
     * @since 1.0.0
     */
    public List<MyLegStep> getSteps() {
        return steps;
    }

    /**
     * Gives a List including all the steps to get from one waypoint to another.
     *
     * @param steps a List of {@link LegStep}.
     * @since 2.1.0
     */
    public void setSteps(List<MyLegStep> steps) {
        this.steps = steps;
    }

    /**
     * An {@link LegAnnotation} that contains additional details about each line segment along the route geometry. If
     * you'd like to receiving this, you must request it inside your Directions request before executing the call.
     *
     * @return a {@link LegAnnotation} object.
     * @since 2.1.0
     */
    public LegAnnotation getAnnotation() {
        return annotation;
    }

}
