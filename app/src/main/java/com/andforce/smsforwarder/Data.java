package com.andforce.smsforwarder;

import org.json.*;


public class Data {
	
    private Aps aps;
    
    
	public Data () {
		
	}	
        
    public Data (JSONObject json) {
    
        this.aps = new Aps(json.optJSONObject("aps"));

    }
    
    public Aps getAps() {
        return this.aps;
    }

    public void setAps(Aps aps) {
        this.aps = aps;
    }


    
}
