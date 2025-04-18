package com.example.loginscreen.Model;
public class HelperClass {

    String name, email, username,password,profileImageUrl,gender,phone;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getGender() {
        return gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }


    public HelperClass(String name, String email, String username,String password,String profileImageUrl) {
        this.name = name;
        this.email = email;
        this.username = username;
        this.password = password;
        this.profileImageUrl = profileImageUrl;
    }

    public HelperClass() {
    }
}