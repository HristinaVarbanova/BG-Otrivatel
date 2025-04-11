package com.example.loginscreen;

public class FriendInfo {
    private String username;
    private int stars;

    public FriendInfo() {} // Празен за Firestore (ако се използва)

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
