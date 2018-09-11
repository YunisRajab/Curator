package com.yunisrajab.curator;


public class History {

    private String title;
    private String url;
    private String time;

    public History(String title, String url, String time) {
        this.title = title;
        this.url = url;
        this.time   =   time;
    }

    public String getTime() {
        return time;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public History    ()  {}
}
