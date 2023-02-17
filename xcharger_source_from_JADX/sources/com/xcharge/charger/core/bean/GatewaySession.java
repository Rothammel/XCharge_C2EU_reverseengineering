package com.xcharge.charger.core.bean;

import com.xcharge.common.bean.JsonBean;

public class GatewaySession extends JsonBean<GatewaySession> {
    private IndicateSession indicateSession = null;
    private RequestSession requestSession = null;

    public RequestSession getRequestSession() {
        return this.requestSession;
    }

    public void setRequestSession(RequestSession requestSession2) {
        this.requestSession = requestSession2;
    }

    public IndicateSession getIndicateSession() {
        return this.indicateSession;
    }

    public void setIndicateSession(IndicateSession indicateSession2) {
        this.indicateSession = indicateSession2;
    }
}
