package com.example.hikermanagementapp;

public class Observation {
    private int id;
    private int trip_id;
    private String type;
    private String time;
    private String note;
    private String image;

    public Observation(int id, int trip_id, String type, String time, String note, String image) {
        this.id = id;
        this.trip_id = trip_id;
        this.type = type;
        this.time = time;
        this.note = note;
        this.image = image;
    }

    public Observation(int trip_id, String type, String time, String note, String image) {
        this.trip_id = trip_id;
        this.type = type;
        this.time = time;
        this.note = note;
        this.image = image;
    }

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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}