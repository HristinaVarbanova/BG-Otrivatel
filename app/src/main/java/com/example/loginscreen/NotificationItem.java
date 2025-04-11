package com.example.loginscreen;

public class NotificationItem {
    private String type;      // например "friend_request"
    private String from;      // имейл или име
    private String fromUid;   // UID на подателя
    private String message;   // съобщение

    // Празен конструктор за Firebase
    public NotificationItem() {}

    // Конструктор с всички полета
    public NotificationItem(String type, String from, String fromUid, String message) {
        this.type = type;
        this.from = from;
        this.fromUid = fromUid;
        this.message = message;
    }

    // Гетъри
    public String getType() {
        return type;
    }

    public String getFrom() {
        return from;
    }

    public String getFromUid() {
        return fromUid;
    }

    public String getMessage() {
        return message;
    }

    // Сетър само за message, ако ти трябва
    public void setMessage(String message) {
        this.message = message;
    }
}
