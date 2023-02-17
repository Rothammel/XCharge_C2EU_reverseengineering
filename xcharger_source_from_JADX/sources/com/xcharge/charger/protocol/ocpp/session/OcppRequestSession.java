package com.xcharge.charger.protocol.ocpp.session;

import com.xcharge.common.bean.JsonBean;
import org.json.JSONArray;

public class OcppRequestSession extends JsonBean<OcppRequestSession> {
    private JSONArray request = null;
    private JSONArray response = null;

    public JSONArray getRequest() {
        return this.request;
    }

    public void setRequest(JSONArray request2) {
        this.request = request2;
    }

    public JSONArray getResponse() {
        return this.response;
    }

    public void setResponse(JSONArray response2) {
        this.response = response2;
    }
}
