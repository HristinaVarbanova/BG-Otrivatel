package com.example.loginscreen;

public class ImageData {
    private String imageUrl;

    // 🔹 Задължителен празен конструктор за Firebase
    public ImageData() {}

    // 🔹 Основен конструктор
    public ImageData(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    // 🔹 Гетър
    public String getImageUrl() {
        return imageUrl;
    }

    // 🔹 Сетър (ако ще ползваш с Firestore/Gson)
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
