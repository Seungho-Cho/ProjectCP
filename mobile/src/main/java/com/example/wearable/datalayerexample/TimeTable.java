package com.example.wearable.datalayerexample;

/**
 * Created by june on 2015-05-05.
 */
public class TimeTable {

    int hour;
    int minute;
    String className;
    String day;


    public String getDay() { return day; }

    public void setDay(String day) { this.day = day; }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }


    public TimeTable(String day, int hour, int minute, String className){
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.className = className;
    }

}
