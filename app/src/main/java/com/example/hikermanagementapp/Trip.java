package com.example.hikermanagementapp;

public class Trip {
    private int id;
    private int user_id;
    private String name;
    private String destination;
    private String date;
    private boolean require_parking;
    private double length;
    private String difficulty;
    private String description;

    public Trip(int trip_id, int user_id, String name, String destination, String date,
                boolean require_parking, double length, String difficulty, String description) {
        this.id = trip_id;
        this.user_id = user_id;
        this.name = name;
        this.destination = destination;
        this.date = date;
        this.require_parking = require_parking;
        this.length = length;
        this.difficulty = difficulty;
        this.description = description;
    }

    public Trip(int user_id, String name, String destination, String date,
                boolean require_parking, double length, String difficulty, String description) {
        this.user_id = user_id;
        this.name = name;
        this.destination = destination;
        this.date = date;
        this.require_parking = require_parking;
        this.length = length;
        this.difficulty = difficulty;
        this.description = description;
    }

    public int getTripId() {
        return id;
    }

    public void setTripId(int trip_id) {
        this.id = trip_id;
    }

    public int getUserId() {
        return user_id;
    }

    public void setUserId(int user_id) {
        this.user_id = user_id;
    }

    public String getTripName() {
        return name;
    }

    public void setTripName(String name) {
        this.name = name;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isRequireParking() {
        return require_parking;
    }

    public void setRequireParking(boolean require_parking) {
        this.require_parking = require_parking;
    }

    public double getLength() {
        return length;
    }

    public void setLengthKm(double length) {
        this.length = length;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
