package com.example.gurpreetsingh.encircleme;

import android.graphics.Bitmap;

/**
 * Created by Brayden on 4/28/2017.
 *
 * This class represents an object that can be used with the UserProfileListAdapter
 * to display user's in a ListView with their profile image. Note that the objects
 * made with this class need to be put into an ArrayList for the adapter to work.
 */

public class UserWithImage {
    private String userID;
    private String username;
    private String name;
    private Bitmap profileImage;

    public UserWithImage(String userID, String username, Bitmap profileImage){
        this.userID = userID;
        this.username = username;
        this.profileImage = profileImage;
    }

    public UserWithImage(String userID, String username, String name, Bitmap profileImage){
        this.userID = userID;
        this.username = username;
        this.name = name;
        this.profileImage = profileImage;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Bitmap getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(Bitmap profileImage) {
        this.profileImage = profileImage;
    }
}
