package com.example.wearable.datalayerexample;

/**
 * Created by on 2015-04-19.
 */
public class GNode {
    int id;
    int category;
    String Name;
    String[] keywords;
    double lon, lat;

    public GNode(int id, int category, String Name, String[] keywords, double lon, double lat) {
        this.id = id;
        this.category = category;
        this.Name = Name;
        this.keywords = keywords;
        this.lon = lon;
        this.lat = lat;
    }
}
