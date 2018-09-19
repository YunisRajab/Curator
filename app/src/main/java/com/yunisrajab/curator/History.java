package com.yunisrajab.curator;


public class History {

    private String title;
    private String id;
    private String time;

    public History(String title, String id, String time) {
        this.title = title;
        this.id = id;
        this.time   =   time;
    }

    public String getTime() {
        return time;
    }

    public String getTitle() {
        return title;
    }

    public String getId() {
        return id;
    }

    public History    ()  {}
}
