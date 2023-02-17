package com.xcharge.charger.protocol.monitor.session;

import com.xcharge.charger.protocol.monitor.bean.YZXDCAPMessage;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class MonitorRequestSession extends JsonBean<MonitorRequestSession> {
    private YZXDCAPMessage request = null;
    private YZXDCAPMessage response = null;

    public YZXDCAPMessage getRequest() {
        return this.request;
    }

    public void setRequest(YZXDCAPMessage request) {
        this.request = request;
    }

    public YZXDCAPMessage getResponse() {
        return this.response;
    }

    public void setResponse(YZXDCAPMessage response) {
        this.response = response;
    }
}