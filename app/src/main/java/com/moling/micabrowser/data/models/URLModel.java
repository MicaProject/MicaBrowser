package com.moling.micabrowser.data.models;

public class URLModel {
    private String title;
    private String url;
    public URLModel(String url, String title) {
        this.url = url;
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }
}
