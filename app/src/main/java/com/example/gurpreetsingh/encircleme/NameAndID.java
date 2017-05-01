package com.example.gurpreetsingh.encircleme;

/**
 * Created by Brayden on 4/30/2017.
 *
 * This class represents an object that can be saved and retrieved from
 * the Firebase database. It holds two String fields of name and id.
 */

public class NameAndID {
    private String name;
    private String id;

    public NameAndID(){} // Default constructor for Firebase database

    public NameAndID(String name, String id){
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getID() {
        return id;
    }

    public void setID(String id) {
        this.id = id;
    }
}
