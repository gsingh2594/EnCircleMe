package com.example.gurpreetsingh.encircleme;

import java.util.ArrayList;

/**
 * Created by Brayden on 2/18/2017.
 */

public class User {
    private String name;
    private String phone;
    private String username;
    private String bio;
    private ArrayList<String> interests;

    // default constructor required for firebase database
    public User(){}

    public User(String name, String phone, String username, ArrayList<String> interestsList){
        this.name = name;
        this.phone = phone;
        this.username = username;
        this.interests = interestsList;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public void setPhone(String phone){
        this.phone = phone;
    }

    public String getPhone(){
        return phone;
    }

    public void setBio(String description){
        this.bio = description;
    }

    public String getBio(){
        return bio;
    }

    public void setUsername(String username){this.username = username;}

    public String getUsername() {
        return username;
    }

    public ArrayList<String> getInterests(){
        return interests;
    }

    public void setInterests(ArrayList<String> interestsList){
        this.interests = interestsList;
    }
}
