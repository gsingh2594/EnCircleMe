package com.example.gurpreetsingh.encircleme;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Brayden on 3/29/2017.
 */

class Event {
    private String name;
    private String about;
    private String date;
    private String startTime;
    private String endTime;
    private double latitude;
    private double longitude;


    public Event(){}

    public Event(String name, String about, String date, String startTime, String endTime, double latitude, double longitude){
        this.name = name;
        this.about = about;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {this.latitude = latitude;}

    public double getLongitude(){ return longitude; }

    public void setLongitude(double longitude){ this.longitude = longitude;}
}
