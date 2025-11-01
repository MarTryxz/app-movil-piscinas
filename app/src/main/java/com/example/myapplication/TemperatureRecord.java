package com.example.myapplication;

public class TemperatureRecord {
    private String date;
    private float temperature;

    public TemperatureRecord(String date, float temperature) {
        this.date = date;
        this.temperature = temperature;
    }

    public String getDate() {
        return date;
    }

    public float getTemperature() {
        return temperature;
    }

    @Override
    public String toString() {
        return "Date: " + date + ", Temperature: " + temperature;
    }
}
