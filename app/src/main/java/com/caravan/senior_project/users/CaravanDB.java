package com.caravan.senior_project.users;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Jeffrey on 6/10/2017.
 */

public class CaravanDB {
    public static DatabaseReference root = FirebaseDatabase.getInstance().getReference();
    public static DatabaseReference roomKeys = root.child("roomKeys");
    public static DatabaseReference rooms = root.child("rooms");
    public static DatabaseReference users = root.child("users");

    private static CaravanDB caravanDB;
    private static RoomManager rm;

    private CaravanDB() {
        rm = new RoomManager();
    }

    public static CaravanDB getCaravanDB() {
        if (caravanDB == null)
            caravanDB = new CaravanDB();
        return caravanDB;
    }
}
