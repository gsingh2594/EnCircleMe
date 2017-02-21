package com.example.gurpreetsingh.encircleme;

/**
 * Created by Brayden on 2/18/2017.
 */

public class User {
    private String name;
    private String phone;
    private String bio;

    // default constructor required for firebase database
    public User(){}

    public User(String name, String phone){
        this.name = name;
        this.phone = phone;
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
}
