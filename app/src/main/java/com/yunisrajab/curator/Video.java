package com.yunisrajab.curator;

import java.io.Serializable;
import java.util.HashMap;

public class Video  implements Serializable {

    private  String title;
    private String id;
    private int rating;

    public Video(String title, String id, int rating) {
        this.title = title;
        this.rating = rating;
        this.id = id;
    }

    public String getTitle() {return title;}

    public int getRating() {return rating;}

    public String getID() {return id;}

    public void setRating   (int    rating) {this.rating    =   rating;}

    public void setUrl(String id) {
        this.id = id;
    }

    public Video    ()  {}
}
