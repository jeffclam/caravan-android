package com.caravan.senior_project.caravan_android;

/**
 * Created by Jeffrey on 5/2/2017.
 */

public class Coord {
    double latitude;
    double longitude;

    public Coord() {
        //Empty Constructor
    }

    public Coord(double lat, double lon) {
        this.latitude = lat;
        this.longitude = lon;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String toString() {
        return "LatLon: <" + getLatitude() + ", " + getLongitude() + ">";
    }
}
