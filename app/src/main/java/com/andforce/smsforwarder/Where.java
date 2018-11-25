package com.andforce.smsforwarder;

import org.json.*;


public class Where {
	
    private String deviceType;
    
    
	public Where () {
		
	}	
        
    public Where (JSONObject json) {
    
        this.deviceType = json.optString("deviceType");

    }
    
    public String getDeviceType() {
        return this.deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }


    
}
