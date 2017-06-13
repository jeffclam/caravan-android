package com.caravan.senior_project.users;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.caravan.senior_project.caravan_android.R;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Jeffrey on 6/9/2017.
 */

public class Room {
    @Exclude private String roomKey;
    @Exclude private ArrayList<Roommate> roommates;
    @Exclude private DatabaseReference room;
    @Exclude private String self;
    private ArrayList<Double> finish;
    private Map<String, String> users;

    public Room() {
    }

    public void setFinish(double lat, double lon) {
        finish.set(0, lat);
        finish.set(1, lon);
    }

    public ArrayList<Double> getFinish() {
        return finish;
    }

    public Map<String, String> getUsers() {
        return users;
    }

    public String getRoomKey() {
        return roomKey;
    }

    public void setRoomKey(String roomKey) {
        this.roomKey = roomKey;
    }

    public ArrayList<Roommate> getRoommates() {
        if (roommates == null) {
            roommates = new ArrayList<>();
        }

        if (!users.isEmpty()) {
            for (Map.Entry<String, String> entry : users.entrySet()) {
                if (!roommates.isEmpty()) {
                    boolean exists = false;
                    for (Roommate r : roommates) {
                        if (entry.getValue().equals(r.getUID()) && !entry.getValue().equals(self)) {
                            exists = true;
                        }
                    }

                    if (!exists) {
                        roommates.add(new Roommate(entry));
                    }
                } else {
                    if (!entry.getValue().equals(self))
                        roommates.add(new Roommate(entry));
                }
            }
        }

        return roommates;
    }

    public void showRoommates(MapboxMap map, Activity activity) {
        Log.d("Room", "showRoommates() called");
        if (roommates == null) {
            getRoommates();
        }

        if (roommates.size() == 1) {
            Toast.makeText(activity, "You have no roommates.", Toast.LENGTH_SHORT).show();
            return;
        } else
                Log.d("Room", "You have " + roommates.size() + " roommates");

        for (int i = 0; i < roommates.size(); i++) {
            Roommate r = roommates.get(i);
            if (r.getIcon() == null) {
                r.setIcon(map.addMarker(new MarkerViewOptions()
                    .position(new LatLng(r.getLatitude(), r.getLongtitude()))));
                IconFactory iconFactory = IconFactory.getInstance(activity);
                Icon icon;
                switch (i % 3) {
                    case 1:
                        Log.d("Room", "draw dis1");
                        icon = iconFactory.fromResource(R.drawable.friend2);
                        break;
                    case 2:
                        Log.d("Room", "draw dis2");
                        icon = iconFactory.fromResource(R.drawable.friend3);
                        break;
                    case 0:
                    default:
                        Log.d("Room", "draw dis 3");
                        icon = iconFactory.fromResource(R.drawable.friend1);

                }
                r.getIcon().setIcon(icon);
            } else {
                r.getIcon().setPosition(new LatLng(r.getLatitude(), r.getLongtitude()));
            }
        }
    }

    public void setRoommates(ArrayList<Roommate> roommates) {
        this.roommates = roommates;
    }

    public void addRoommate(User user) {
        if (roomKey != null) {
            if (room == null)
                room = CaravanDB.rooms.child(roomKey);

            self = user.getUID();

            boolean exists = false;
            for (Map.Entry<String, String> e : users.entrySet()) {
                if (e.getValue().equals(user.getUID())) {
                    exists = true;
                }
            }

            if (!exists)
                room.child("users").push().setValue(user.getUID());
        }
    }

    public void pushToDB() {
        if (roomKey != null) {
            if (room == null)
                room = CaravanDB.rooms.child(roomKey);
            room.child("finish").setValue(finish);
            room.child("users").setValue(users);
        } else {
            Log.d("Room", "No room to push");
        }
    }
}
