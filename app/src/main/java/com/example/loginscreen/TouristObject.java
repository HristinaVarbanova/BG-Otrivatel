package com.example.loginscreen;
import android.util.Log;

import java.io.Serializable;

public class TouristObject implements  Serializable{
    private String name;
    private String Info;
    private String Location;
    private String image;
    private long visitTime;

    // Празен конструктор (задължителен за Firebase)
    public TouristObject() {
    }

    public TouristObject(String name, String info, String location, String image) {
        this.name = name;
        this.Info = info;
        this.Location = location;
        this.image = image;
    }

    public TouristObject(String name, String info, String location) {
        this.name = name;
        this.Info = info;
        this.Location = location;
    }


    public String getName() {
        return name;
    }

    public String getInfo() {
        return Info;
    }

    public String getLocation() {
        return Location;
    }

    public String getImage() {
        return image;
    }

    public void setName(String name) {
        Log.d("TouristObject", "Задаване на име: " + name);
        this.name = name;
    }

    public void setInfo(String info) {
        this.Info = info;
    }

    public void setLocation(String location) {
        this.Location = location;
    }

    public void setImage(String image) {
        this.image = image;   }

    public long getVisitTime() {
        return visitTime;
    }

    public void setVisitTime(long visitTime) {
        this.visitTime = visitTime;
    }
}
