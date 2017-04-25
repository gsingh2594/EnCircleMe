package com.example.gurpreetsingh.encircleme;

import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Brayden on 3/29/2017.
 */

class Event {
    private String name;
    private String about;
    private String date;
    private String startTime;
    private String endDate;
    private String endTime;
    @Nullable private String placeID;
    @Nullable private String placeName;
    @Nullable String address;
    private double latitude;
    private double longitude;


    public Event(){} // Empty constructor for FirebaseDatabase

    public Event(String name, String about, String date, String startTime, String endDate, String endTime,
                 String placeID, String placeName, String address, double latitude, double longitude){
        this.name = name;
        this.about = about;
        this.date = date;
        this.startTime = startTime;
        this.endDate = endDate;
        this.endTime = endTime;
        this.placeID = placeID; // could be null
        this.placeName = placeName; // could be null
        this.address = address; // could be null
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

    public String getEndDate(){ return endDate;}

    public void setEndDate(String date){this.endDate = date;}

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getPlaceID(){return placeID;}

    public void setPlaceID(String placeID){this.placeID = placeID;}

    public String getPlaceName(){ return placeName;}

    public void setPlaceName(String placeName){this.placeName = placeName;}

    public String getAddress(){return address;}

    public void setAddress(String address){this.address = address;}

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {this.latitude = latitude;}

    public double getLongitude(){ return longitude; }

    public void setLongitude(double longitude){ this.longitude = longitude;}
}
