package com.andforce.smsforwarder;

import org.json.*;


public class PushData {
	
    private Where where;
    private Data data;
    private String prod;
    
    
	public PushData () {
		
	}	
        
    public PushData (JSONObject json) {
    
        this.where = new Where(json.optJSONObject("where"));
        this.data = new Data(json.optJSONObject("data"));
        this.prod = json.optString("prod");

    }
    
    public Where getWhere() {
        return this.where;
    }

    public void setWhere(Where where) {
        this.where = where;
    }

    public Data getData() {
        return this.data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public String getProd() {
        return this.prod;
    }

    public void setProd(String prod) {
        this.prod = prod;
    }


    
}
