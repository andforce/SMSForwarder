package com.andforce.smsforwarder;

import org.json.*;


public class Sms {
	
    private String person;
    private String protocol;
    private String address;
    private String id;
    private String date;
    private String read;
    private String type;
    private String body;
    private String simId;
    
    
	public Sms () {
		
	}	
        
    public Sms (JSONObject json) {
    
        this.person = json.optString("person");
        this.protocol = json.optString("protocol");
        this.address = json.optString("address");
        this.id = json.optString("id");
        this.date = json.optString("date");
        this.read = json.optString("read");
        this.type = json.optString("type");
        this.body = json.optString("body");
        this.simId = json.optString("sim_id");

    }
    
    public String getPerson() {
        return this.person;
    }

    public void setPerson(String person) {
        this.person = person;
    }

    public String getProtocol() {
        return this.protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return this.date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getRead() {
        return this.read;
    }

    public void setRead(String read) {
        this.read = read;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBody() {
        return this.body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getSimId() {
        return this.simId;
    }

    public void setSimId(String simId) {
        this.simId = simId;
    }


    
}
