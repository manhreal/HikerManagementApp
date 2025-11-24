package com.example.hikermanagementapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Database extends SQLiteOpenHelper {
    private static final String DB_NAME = "m_hike";
    private static final int DB_VER = 1;

    // Users table
    private static final String TABLE_USERS = "users";
    private static final String COL_USER_ID = "id";
    private static final String COL_USER_EMAIL = "email";
    private static final String COL_USER_PASSWORD = "password";

    // Trips table
    private static final String TABLE_TRIPS = "trips";
    private static final String COL_TRIP_ID = "id";
    private static final String COL_TRIP_USER_ID = "user_id";
    private static final String COL_TRIP_NAME = "name";
    private static final String COL_TRIP_DESTINATION = "destination";
    private static final String COL_TRIP_DATE = "date";
    private static final String COL_TRIP_REQUIRE_PARKING = "require_parking";
    private static final String COL_TRIP_LENGTH = "length";
    private static final String COL_TRIP_DIFFICULTY = "difficulty";
    private static final String COL_DESCRIPTION = "description";

    // Observations table
    private static final String TABLE_OBSERVATIONS = "observations";
    private static final String COL_OBS_ID = "id";
    private static final String COL_OBS_TRIP_ID = "trip_id";
    private static final String COL_OBS_TYPE = "type";
    private static final String COL_OBS_TIME = "time";
    private static final String COL_OBS_NOTE = "note";

    // Init
    public Database(Context ct) {
        super(ct, DB_NAME, null, DB_VER);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // create users table
        String create_users_table = "CREATE TABLE " + TABLE_USERS + " ("
                + COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_USER_EMAIL + " TEXT UNIQUE NOT NULL, "
                + COL_USER_PASSWORD + " TEXT NOT NULL)";
        db.execSQL(create_users_table);

        // create trips table
        String create_trips_table = "CREATE TABLE " + TABLE_TRIPS + " ("
                + COL_TRIP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_TRIP_USER_ID + " INTEGER, "
                + COL_TRIP_NAME + " TEXT NOT NULL, "
                + COL_TRIP_DESTINATION + " TEXT NOT NULL, "
                + COL_TRIP_DATE + " TEXT NOT NULL, "
                + COL_TRIP_REQUIRE_PARKING + " INTEGER DEFAULT 0, "
                + COL_TRIP_LENGTH + " REAL, "
                + COL_TRIP_DIFFICULTY + " TEXT, "
                + COL_DESCRIPTION + " TEXT, "
                + "FOREIGN KEY(" + COL_TRIP_USER_ID + ") REFERENCES "
                + TABLE_USERS + "(" + COL_USER_ID + "))";
        db.execSQL(create_trips_table);

        // create observations table
        String create_observations_table = "CREATE TABLE " + TABLE_OBSERVATIONS + " (" +
                COL_OBS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_OBS_TRIP_ID + " INTEGER, "
                + COL_OBS_TYPE + " TEXT, "
                + COL_OBS_TIME + " TEXT, "
                + COL_OBS_NOTE + " TEXT, "
                + "FOREIGN KEY(" + COL_OBS_TRIP_ID + ") REFERENCES "
                + TABLE_TRIPS + "(" + COL_TRIP_ID + "))";
        db.execSQL(create_observations_table);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_OBSERVATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRIPS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    public boolean registerUser(String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(COL_USER_EMAIL, email);
        values.put(COL_USER_PASSWORD, password);

        long result = db.insert(TABLE_USERS, null, values);
        return result != 1;
    }

    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COL_USER_ID},
                COL_USER_EMAIL + "=? AND " + COL_USER_PASSWORD + "=?",
                new String[]{email, password},
                null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public  int getUserIdByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COL_USER_ID},
                COL_USER_EMAIL + "=?",
                new String[]{email},
                null, null, null);

        int userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(0);
        }
        cursor.close();
        return userId;
    }

    // Trips function
    public long addTrip(int user_id, String name, String destination, String date, boolean require_parking,
                        double length, String difficulty, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COL_TRIP_USER_ID, user_id);
        values.put(COL_TRIP_NAME, name);
        values.put(COL_TRIP_DESTINATION, destination);
        values.put(COL_TRIP_DATE, date);
        values.put(COL_TRIP_REQUIRE_PARKING, require_parking ? 1 : 0);
        values.put(COL_TRIP_LENGTH, length);
        values.put(COL_TRIP_DIFFICULTY, difficulty);
        values.put(COL_DESCRIPTION, description);

        return db.insert(TABLE_TRIPS, null, values);
    }

    public Cursor getTripById(int trip_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                TABLE_TRIPS,
                null,
                COL_TRIP_ID + "=?",
                new String[]{String.valueOf(trip_id)},
                null, null, null
        );
    }

    public Cursor getAllTripsByUser(int user_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_TRIPS, null,
                COL_TRIP_USER_ID + "=?",
                new String[]{String.valueOf(user_id)},
                null, null, COL_TRIP_DATE + " DESC");
    }

    public Cursor searchTripByName(int user_id, String keyword) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_TRIPS, null,
                COL_TRIP_USER_ID + "=? AND " + COL_TRIP_NAME + " LIKE ?",
                new String[]{String.valueOf(user_id), "%" + keyword + "%"},
                null, null, COL_TRIP_DATE + " DESC");
    }

    public boolean deleteTrip(int trip_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        // delete observations first
        db.delete(TABLE_OBSERVATIONS, COL_OBS_TRIP_ID + "=?",
                new String[]{String.valueOf(trip_id)});
        // delete trip
        int result = db.delete(TABLE_TRIPS, COL_TRIP_ID + "=?",
                new String[]{String.valueOf(trip_id)});
        return result > 0;
    }

    // observation function
    public long addObservation(int trip_id, String type, String time, String note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_OBS_TRIP_ID, trip_id);
        values.put(COL_OBS_TYPE, type);
        values.put(COL_OBS_TIME, time);
        values.put(COL_OBS_NOTE, note);

        return db.insert(TABLE_OBSERVATIONS, null, values);
    }

    public Cursor getObservations(int trip_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                TABLE_OBSERVATIONS,
                null,
                COL_OBS_TRIP_ID + "=?",
                new String[]{String.valueOf(trip_id)},
                null, null,
                COL_OBS_TIME + " ASC"
        );
    }


    public Cursor getObservationByTrip(int trip_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_OBSERVATIONS, null,
                COL_OBS_TRIP_ID + "=?",
                new String[]{String.valueOf(trip_id)},
                null, null, COL_OBS_TIME + " ASC");
    }
}
