package com.caravan.senior_project.users;

/**
 * Created by Jeffrey on 5/2/2017.
 */

public class Coord {
    private double latitude;
    private double longitude;

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

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String toString() {
        return "LatLon: <" + getLatitude() + ", " + getLongitude() + ">";
    }
}
