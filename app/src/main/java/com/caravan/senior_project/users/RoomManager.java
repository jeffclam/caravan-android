package com.caravan.senior_project.users;

import android.app.Activity;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import java.util.ArrayList;

/**
 * Created by Jeffrey on 6/10/2017.
 */

public class RoomManager {
    private String TAG = "RoomManager";

    private Room room;
    private ArrayList<Roommate> roommates;
    private DatabaseReference db_room;

    public RoomManager() {
    }

    public void readRoom(final String roomKey, final User user) {
        Log.d(TAG, "readRoom(): trying");
        ValueEventListener roomListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    if (dataSnapshot.exists()) {
                        room = dataSnapshot.getValue(Room.class);
                        room.setRoomKey(roomKey);
                        room.addRoommate(user);
                        room.getRoommates();
                        Log.d(TAG, "readRoom(): Successfully pulled a room.");
                    } else {
                        room = null;
                    }
                } catch (DatabaseException d) {
                    Log.e(TAG, "Error in readRoom():" + d.getMessage());
                    d.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        if (db_room == null) {
            db_room = CaravanDB.rooms.child(roomKey);
        }
        db_room.addValueEventListener(roomListener);
    }

    public void showRoommates(MapboxMap map, Activity activity) {
        if (room != null) {
            Log.d(TAG, "showRoommates called!!");
            room.showRoommates(map, activity);
        } else {
            Log.e(TAG,"Room is null");
        }
    }

    public Room getRoom() {
        return room;
    }

    public void getRoommates() {
        if (room != null) {
            room.getUsers();
        } else {
            Log.d(TAG, "getRoommates(): no room reference");
        }
    }
}
