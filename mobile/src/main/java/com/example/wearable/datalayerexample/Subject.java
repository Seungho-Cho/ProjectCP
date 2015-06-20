package com.example.wearable.datalayerexample;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by june on 2015-05-04.
 */
public class Subject {

    ArrayList<String> day_list;
    ArrayList<Integer> time_list;
    ArrayList<String> subejct_name_list;
    ArrayList<Integer> subject_no_list;

    public ArrayList<Integer> getSubject_no_list() {
        return subject_no_list;
    }

    public void setSubject_no_list(ArrayList<Integer> subject_no_list) {
        this.subject_no_list = subject_no_list;
    }

    public ArrayList<String> getDay_list() {
        return day_list;
    }

    public void setDay_list(ArrayList<String> day_list) {
        this.day_list = day_list;
    }

    public ArrayList<Integer> getTime_list() {
        return time_list;
    }

    public void setTime_list(ArrayList<Integer> time_list) {
        this.time_list = time_list;
    }

    public ArrayList<String> getSubejct_name_list() {
        return subejct_name_list;
    }

    public void setSubejct_name_list(ArrayList<String> subejct_name_list) {
        this.subejct_name_list = subejct_name_list;
    }




}
