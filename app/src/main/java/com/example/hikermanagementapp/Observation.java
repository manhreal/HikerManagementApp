package com.example.hikermanagementapp;

public class Observation {
    private int id;
    private int trip_id;
    private String type;
    private String time;
    private String note;

    public Observation(int id, int trip_id, String type, String time, String note) {
        this.id = id;
        this.trip_id = trip_id;
        this.type = type;
        this.time = time;
        this.note = note;
    }

    public Observation(int trip_id, String type, String time, String note) {
        this.trip_id = trip_id;
        this.type = type;
        this.time = time;
        this.note = note;
    }

    // Getters và Setters
    public int getObservationId() {
        return id;
    }

    public void setObservationId(int observation_id) {
        this.id = observation_id;
    }

    public int getTripId() {
        return trip_id;
    }

    public void setTripId(int trip_id) {
        this.trip_id = trip_id;
    }

    public String getObservationType() {
        return type;
    }

    public void setObservationType(String type) {
        this.type = type;
    }

    public String getObservationTime() {
        return time;
    }

    public void setObservationTime(String time) {
        this.time = time;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}