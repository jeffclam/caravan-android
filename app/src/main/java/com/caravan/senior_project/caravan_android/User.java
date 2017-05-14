package com.caravan.senior_project.caravan_android;

import com.mapbox.services.directions.v5.models.DirectionsRoute;

/**
 * Created by Jeffrey on 4/10/2017.
 */

public class User {
    public class Coord {
        double latitude;
        double longitude;
    }

    public String username;
    public String email;
    public Coord coord ;
    public DirectionsRoute route;

    public User(String username, String email) {
        this.username = username;
        this.email = email;
        coord = new Coord();
    }

    public void setCoords(double lat, double lon) {
        coord.latitude = lat;
        coord.longitude = lon;
    }

    public void setRoute(DirectionsRoute r) {
        route = r;
    }
}
