package com.andforce.smsforwarder;

import org.json.*;


public class Alert {

    private String title;
    private String body;


    public Alert() {

    }

    public Alert(JSONObject json) {

        this.title = json.optString("title");
        this.body = json.optString("body");

    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return this.body;
    }

    public void setBody(String body) {
        this.body = body;
    }


}
