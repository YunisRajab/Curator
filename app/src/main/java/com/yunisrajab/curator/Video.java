package com.yunisrajab.curator;

public class Video {

    private  String title;
    private String url;
    private int rating;

    public Video(String title, String url, int rating) {
        this.title = title;
        this.rating = rating;
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public int getRating() {
        return rating;
    }

    public String getUrl() {
        return url;
    }

    public Video    ()  {}
}
