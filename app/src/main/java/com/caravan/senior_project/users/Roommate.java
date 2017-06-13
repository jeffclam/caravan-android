package com.caravan.senior_project.users;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.mapboxsdk.annotations.MarkerView;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Jeffrey on 6/10/2017.
 */

public class Roommate {
    private User roommate;
    private MarkerView icon;
    private ValueEventListener locationListener;
    private DatabaseReference db_location;

    public Roommate() {

    }

    public Roommate(Map.Entry<String, String> entry) {
        roommate = new User(entry.getValue());
        db_location = CaravanDB.users.child(roommate.getUID()).child("location");
        locationListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                roommate.setLocation((Double) dataSnapshot.child("0").getValue(),
                        (Double) dataSnapshot.child("1").getValue());
                if (icon != null) {
                    icon.setPosition(new LatLng(getLatitude(), getLongtitude()));
                }
                Log.d("Roommate", "new Roommate(): " + getUID()
                        + " <" + getLatitude() + ", " + getLongtitude() + ">");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Roommate", databaseError.getMessage());
            }
        };
        db_location.addValueEventListener(locationListener);
    }

    public User getRoommate() {
        return roommate;
    }

    public void setRoommate(User roommate) {
        this.roommate = roommate;
    }

    public MarkerView getIcon() {
        return icon;
    }

    public void setIcon(MarkerView icon) {
        this.icon = icon;
    }

    public String getUID() {
        return roommate.getUID();
    }

    public double getLatitude() {
        return roommate.getLatitude();
    }

    public double getLongtitude() {
        return roommate.getLongitude();
    }
}
