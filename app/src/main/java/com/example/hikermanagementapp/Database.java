package com.example.hikermanagementapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class Database extends SQLiteOpenHelper {
    private static final String DB_NAME = "m_hike";
    private static final int DB_VER = 3;

    // Users table
    private static final String TABLE_USERS = "users";
    private static final String COL_USER_ID = "id";
    private static final String COL_USER_EMAIL = "email";
    private static final String COL_USER_PASSWORD = "password";
    private static final String COL_USER_NAME = "name";
    private static final String COL_USER_AVATAR = "avatar";

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
    private static final String COL_OBS_IMAGE = "image"; // NEW COLUMN

    public Database(Context ct) {
        super(ct, DB_NAME, null, DB_VER);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String create_users_table = "CREATE TABLE " + TABLE_USERS + " ("
                + COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_USER_EMAIL + " TEXT UNIQUE NOT NULL, "
                + COL_USER_PASSWORD + " TEXT NOT NULL, "
                + COL_USER_NAME + " TEXT, "
                + COL_USER_AVATAR + " TEXT)";
        db.execSQL(create_users_table);

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

        String create_observations_table = "CREATE TABLE " + TABLE_OBSERVATIONS + " (" +
                COL_OBS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_OBS_TRIP_ID + " INTEGER, "
                + COL_OBS_TYPE + " TEXT, "
                + COL_OBS_TIME + " TEXT, "
                + COL_OBS_NOTE + " TEXT, "
                + COL_OBS_IMAGE + " TEXT, "
                + "FOREIGN KEY(" + COL_OBS_TRIP_ID + ") REFERENCES "
                + TABLE_TRIPS + "(" + COL_TRIP_ID + "))";
        db.execSQL(create_observations_table);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_OBSERVATIONS + " ADD COLUMN " + COL_OBS_IMAGE + " TEXT");
        }
    }

    public boolean emailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COL_USER_ID},
                COL_USER_EMAIL + "=?",
                new String[]{email},
                null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public boolean registerUser(String email, String password, String name, String avatar) {
        SQLiteDatabase db = this.getWritableDatabase();

        if (emailExists(email)) {
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(COL_USER_EMAIL, email);
        values.put(COL_USER_PASSWORD, password);
        values.put(COL_USER_NAME, name);
        values.put(COL_USER_AVATAR, avatar);

        long result = db.insert(TABLE_USERS, null, values);
        return result > 0;
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

    public int getUserIdByEmail(String email) {
        Log.d("DB", "Getting userId for email: " + email);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COL_USER_ID},
                COL_USER_EMAIL + "=?",
                new String[]{email},
                null, null, null);

        int userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(0);
            Log.d("DB", "Found userId: " + userId);
        } else {
            Log.d("DB", "User not found for email: " + email);
        }
        cursor.close();
        return userId;
    }

    public Cursor getUserById(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_USERS,
                new String[]{COL_USER_ID, COL_USER_EMAIL, COL_USER_PASSWORD, COL_USER_NAME, COL_USER_AVATAR},
                COL_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null, null);
    }

    public boolean updateUserName(int userId, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER_NAME, name);

        int result = db.update(TABLE_USERS, values,
                COL_USER_ID + "=?",
                new String[]{String.valueOf(userId)});

        Log.d("DB_UPDATE", "UserId: " + userId + ", Name: " + name + ", Result: " + result);
        return result > 0;
    }

    public boolean updateUserAvatar(int userId, String avatarPath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER_AVATAR, avatarPath);

        int result = db.update(TABLE_USERS, values,
                COL_USER_ID + "=?",
                new String[]{String.valueOf(userId)});
        return result > 0;
    }

    public boolean changePassword(int userId, String oldPassword, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COL_USER_ID},
                COL_USER_ID + "=? AND " + COL_USER_PASSWORD + "=?",
                new String[]{String.valueOf(userId), oldPassword},
                null, null, null);

        if (cursor.getCount() == 0) {
            cursor.close();
            return false;
        }
        cursor.close();

        ContentValues values = new ContentValues();
        values.put(COL_USER_PASSWORD, newPassword);

        int result = db.update(TABLE_USERS, values,
                COL_USER_ID + "=?",
                new String[]{String.valueOf(userId)});
        return result > 0;
    }

    public int getTripCountByUser(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_TRIPS + " WHERE " + COL_TRIP_USER_ID + "=?",
                new String[]{String.valueOf(userId)});

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

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

    public boolean updateTrip(int trip_id, String name, String destination, String date,
                              boolean require_parking, double length, String difficulty, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COL_TRIP_NAME, name);
        values.put(COL_TRIP_DESTINATION, destination);
        values.put(COL_TRIP_DATE, date);
        values.put(COL_TRIP_REQUIRE_PARKING, require_parking ? 1 : 0);
        values.put(COL_TRIP_LENGTH, length);
        values.put(COL_TRIP_DIFFICULTY, difficulty);
        values.put(COL_DESCRIPTION, description);

        int result = db.update(TABLE_TRIPS, values,
                COL_TRIP_ID + "=?",
                new String[]{String.valueOf(trip_id)});

        return result > 0;
    }

    public boolean deleteTrip(int trip_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_OBSERVATIONS, COL_OBS_TRIP_ID + "=?",
                new String[]{String.valueOf(trip_id)});
        int result = db.delete(TABLE_TRIPS, COL_TRIP_ID + "=?",
                new String[]{String.valueOf(trip_id)});
        return result > 0;
    }

    public long addObservation(int trip_id, String type, String time, String note, String imagePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_OBS_TRIP_ID, trip_id);
        values.put(COL_OBS_TYPE, type);
        values.put(COL_OBS_TIME, time);
        values.put(COL_OBS_NOTE, note);
        values.put(COL_OBS_IMAGE, imagePath);

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

    public Cursor getObservationsPaginated(int tripId, int page, int pageSize) {
        SQLiteDatabase db = this.getReadableDatabase();
        int offset = (page - 1) * pageSize;

        String query = "SELECT * FROM " + TABLE_OBSERVATIONS +
                " WHERE " + COL_OBS_TRIP_ID + " = ?" +
                " ORDER BY " + COL_OBS_TIME + " DESC" +
                " LIMIT ? OFFSET ?";

        return db.rawQuery(query, new String[]{
                String.valueOf(tripId),
                String.valueOf(pageSize),
                String.valueOf(offset)
        });
    }

    // Đếm tổng số observations
    public int getObservationsCount(int tripId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_OBSERVATIONS +
                " WHERE " + COL_OBS_TRIP_ID + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(tripId)});
        int count = 0;

        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }

        return count;
    }
}