package com.example.loginscreen.Model;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class NotificationItem {
    private String type;
    private String from;
    private String fromUid;
    private String message;
    private String status;
    private String id;

    @ServerTimestamp
    private Date timestamp;
    public NotificationItem() {}

    public NotificationItem(String type, String from, String fromUid, String message, String status, String id) {
        this.type = type;
        this.from = from;
        this.fromUid = fromUid;
        this.message = message;
        this.status = status;
        this.id = id;
    }

    public NotificationItem(String type, String from, String fromUid, String message) {
        this.type = type;
        this.from = from;
        this.fromUid = fromUid;
        this.message = message;

        if ("friend_request".equals(type)) {
            this.status = "pending";
        }
    }

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

    public String getStatus() {
        return status;
    }

    public String getId() {
        return id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    // ✏️ Сетъри
    public void setMessage(String message) {
        this.message = message;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setFromUid(String fromUid) {
        this.fromUid = fromUid;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
