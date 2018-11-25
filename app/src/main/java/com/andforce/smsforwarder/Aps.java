package com.andforce.smsforwarder;

import org.json.*;


public class Aps {
	
    private Sms sms;
    private Alert alert;
    private String badge;
    private String sound;
    
    
	public Aps () {
		
	}	
        
    public Aps (JSONObject json) {
    
        this.sms = new Sms(json.optJSONObject("sms"));
        this.alert = new Alert(json.optJSONObject("alert"));
        this.badge = json.optString("badge");
        this.sound = json.optString("sound");

    }
    
    public Sms getSms() {
        return this.sms;
    }

    public void setSms(Sms sms) {
        this.sms = sms;
    }

    public Alert getAlert() {
        return this.alert;
    }

    public void setAlert(Alert alert) {
        this.alert = alert;
    }

    public String getBadge() {
        return this.badge;
    }

    public void setBadge(String badge) {
        this.badge = badge;
    }

    public String getSound() {
        return this.sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }


    
}
