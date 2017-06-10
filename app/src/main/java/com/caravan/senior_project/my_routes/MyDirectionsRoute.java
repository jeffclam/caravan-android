package com.caravan.senior_project.my_routes;

import com.google.gson.annotations.SerializedName;
import com.mapbox.services.api.directions.v5.models.DirectionsRoute;
import com.mapbox.services.api.directions.v5.models.RouteLeg;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rohin_000 on 5/17/2017.
 */

public class MyDirectionsRoute {
    private double distance;
    private double duration;
    private String geometry;
    private double weight;
    @SerializedName("weight_name")
    private String weightName;
    private List<MyRouteLeg> legs = new ArrayList<MyRouteLeg>();

    // Empty constructor
    public MyDirectionsRoute(){}

    public MyDirectionsRoute(DirectionsRoute route) {
        this.distance = route.getDistance();
        this.duration = route.getDuration();
        this.geometry = route.getGeometry();
        this.weight = route.getWeight();
        this.weightName = route.getWeightName();
        for (int i = 0; i < route.getLegs().size(); i++) {
            this.legs.add(new MyRouteLeg(route.getLegs().get(i)));
        }
    }

    //public getter fuctions
    public double getDistance() { return distance; }

    public String getWeightName() {
        return weightName;
    }

    public double getWeight() {
        return weight;
    }

    public double getDuration() {
        return duration;
    }

    public String getGeometry() {
        return geometry;
    }

    public List<MyRouteLeg> getLegs() {
        return legs;
    }

    public DirectionsRoute routeToDirectionsRoute() {
        DirectionsRoute route = new DirectionsRoute();
        route.setDistance(this.getDistance());
        route.setDuration(this.getDuration());
        route.setGeometry(this.getGeometry());
        route.setWeight(this.weight);
        route.setWeightName(this.weightName);

        List<RouteLeg> routeLegs = new ArrayList<>();
        for (MyRouteLeg l : this.getLegs()) {
            routeLegs.add(l.legToRouteLeg());
        }
        route.setLegs(routeLegs);
        return route;
    }
}
