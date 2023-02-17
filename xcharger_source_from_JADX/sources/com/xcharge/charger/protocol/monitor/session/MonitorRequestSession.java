package com.xcharge.charger.protocol.monitor.session;

import com.xcharge.charger.protocol.monitor.bean.YZXDCAPMessage;
import com.xcharge.common.bean.JsonBean;

public class MonitorRequestSession extends JsonBean<MonitorRequestSession> {
    private YZXDCAPMessage request = null;
    private YZXDCAPMessage response = null;

    public YZXDCAPMessage getRequest() {
        return this.request;
    }

    public void setRequest(YZXDCAPMessage request2) {
        this.request = request2;
    }

    public YZXDCAPMessage getResponse() {
        return this.response;
    }

    public void setResponse(YZXDCAPMessage response2) {
        this.response = response2;
    }
}
