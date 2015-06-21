package com.example.wearable.datalayerexample;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by june on 2015-04-13.
 */

public class Item implements Serializable {
    private static final long serialVersionUID = 1L;
    private String student_name;
    private String student_number;
    private String major;
    private String subject_name;
    private String day;
    private String subject_no;
    private String lecture_time;
    private String loc_code;

    public Item(String student_name, String student_number, String major, String subject_name, String day, String subject_no, String lecture_time, String loc_code) {
        this.student_name = student_name;
        this.student_number = student_number;
        this.major = major;
        this.subject_name = subject_name;
        this.day = day;
        this.subject_no = subject_no;
        this.lecture_time = lecture_time;
        this.loc_code = loc_code;
    }


    public String getSubject_no() {
        return subject_no;
    }

    public void getSubject_no(String subject_no) {
        this.lecture_time = subject_no;
    }

    public String getLecture_time() {
        return lecture_time;
    }

    public void setLecture_time(String lecture_time) {
        this.lecture_time = lecture_time;
    }

    public String getSubject_name() {
        return subject_name;
    }

    public void setSubject_name(String subject_name) {
        this.subject_name = subject_name;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }



    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getStudent_name() {
        return student_name;
    }

    public void setStudent_name(String student_name) {
        this.student_name = student_name;
    }

    public String getStudent_number() {
        return student_number;
    }

    public void setStudent_number(String student_number) {
        this.student_number = student_number;
    }
    public String getLoc_code() {
        return student_number;
    }

    public void setLoc_code(String student_number) {
        this.student_number = student_number;
    }


}



