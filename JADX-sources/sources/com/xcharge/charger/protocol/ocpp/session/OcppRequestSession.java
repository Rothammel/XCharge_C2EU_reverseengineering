package com.xcharge.charger.protocol.ocpp.session;

import com.xcharge.common.bean.JsonBean;
import org.json.JSONArray;

/* loaded from: classes.dex */
public class OcppRequestSession extends JsonBean<OcppRequestSession> {
    private JSONArray request = null;
    private JSONArray response = null;

    public JSONArray getRequest() {
        return this.request;
    }

    public void setRequest(JSONArray request) {
        this.request = request;
    }

    public JSONArray getResponse() {
        return this.response;
    }

    public void setResponse(JSONArray response) {
        this.response = response;
    }
}
