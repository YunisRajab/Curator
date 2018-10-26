package com.yunisrajab.curator;


import java.io.Serializable;

public class History    implements Serializable {

    private String title;
    private String id;
    private String time;
    private String key;

    public History(String title, String id, String time,    String  key) {
        this.title = title;
        this.id = id;
        this.time   =   time;
        this.key   =   key;
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

    public String getKey() {
        return key;
    }

    public void setUrl(String id) {
        this.id = id;
    }

    public History    ()  {}
}
