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
    private LatLng latLng;


    public Event(String name, String about, String date, String startTime, String endTime, LatLng latLng){
        this.name = name;
        this.about = about;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.latLng = latLng;
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

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }
}
