package com.example.darshanh.photodemoapp;

import java.util.ArrayList;

public class SingletonImageUri {
    private static final SingletonImageUri uniqueInstance=new SingletonImageUri();
    private ArrayList<ImageUri> uri = new ArrayList<>();
    private SingletonImageUri() {
    }
    public static SingletonImageUri getUniqueInstance() {

           return uniqueInstance;
    }

    public ArrayList<ImageUri> getUri() {
        return uri;
    }

    public void setUri(ArrayList<ImageUri> uri) {
        this.uri = uri;
    }
}
