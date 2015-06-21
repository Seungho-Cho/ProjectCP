package com.example.wearable.datalayerexample;

import java.io.Serializable;

/**
 * Created by june on 2015-05-05.
 */
public class TimeTable implements Serializable{

    private static final long serialVersionUID = 1209L;

    private int loc_code;
    private int hour;
    private int minute;
    private String className;
    private String day;



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

    public int getLocCode() { return loc_code; }

    public void setLocCode() { this.loc_code = loc_code; }

    public TimeTable(String day, int hour, int minute, String className, int loc_code){
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.className = className;
        this.loc_code = loc_code;
    }

}
