package com.example.my_application;

public class UserData {
    private String time;
    private String timer;
    private String activity;
    private String description;

    public UserData() {
        // Обязательный конструктор по умолчанию для Firebase
    }

    public UserData(String time, String timer , String activity, String description) {
        this.time = time;
        this.timer = timer;
        this.activity = activity;
        this.description = description;
    }

    public String getTime() {
        return time;
    }

    public String getDescription() {
        return description;
    }

    public String getActivity() {
        return activity;
    }
    public String getTimer(){return timer;}
}

