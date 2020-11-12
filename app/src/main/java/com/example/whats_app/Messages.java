package com.example.whats_app;

public class Messages {
    private String from, message, type;

    public Messages(String from, String message, String type) {
        this.from = from;
        this.message = message;
        this.type = type;
    }

    public Messages() {

    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String mesagee) {
        this.message = mesagee;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
