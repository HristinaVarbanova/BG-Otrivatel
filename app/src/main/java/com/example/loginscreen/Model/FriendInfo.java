package com.example.loginscreen.Model;

public class FriendInfo {
    private String username;
    private int stars;

    public FriendInfo() {}

    public FriendInfo(String username, int stars) {
        this.username = username;
        this.stars = stars;
    }

    public String getUsername() {
        return username;
    }

    public int getStars() {
        return stars;
    }
}
