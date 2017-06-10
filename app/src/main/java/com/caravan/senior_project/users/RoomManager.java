package com.caravan.senior_project.users;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

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

    public void readRoom(String roomKey) {
        Log.d(TAG, "readRoom(): trying");

        ValueEventListener roomListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    room = dataSnapshot.getValue(Room.class);
                    Log.d(TAG, "readRoom(): Successfully pulled a room.");
                } catch (DatabaseException d) {
                    Log.e(TAG, "Error in readRoom():" + d.getMessage());
                    d.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        if (db_room == null)
            db_room = CaravanDB.rooms.child(roomKey);
        db_room.addValueEventListener(roomListener);
    }

    public void getRoommates() {
        if (room != null) {
            room.getUsers();
        } else {
            Log.d(TAG, "getRoommates(): no room reference");
        }
    }
}
