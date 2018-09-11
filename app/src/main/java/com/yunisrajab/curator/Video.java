package com.yunisrajab.curator;

import java.util.HashMap;

public class Video {

    private  String title;
    private String url;
    private int rating;
    private HashMap<String, Boolean> votes;

    public Video(String title, String url, int rating,  HashMap<String, Boolean> votes) {
        this.title = title;
        this.rating = rating;
        this.url = url;
        this.votes   =   votes;
    }

    public String getTitle() {return title;}

    public int getRating() {return rating;}

    public String getUrl() {return url;}

    public HashMap<String, Boolean>  getVote ()  {return votes;}

    public void setVote (HashMap<String,Boolean> vote)   {this.votes  =   vote;}

    public void setRating   (int    rating) {this.rating    =   rating;}

    public Video    ()  {}
}
