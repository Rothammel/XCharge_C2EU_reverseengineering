package com.xcharge.charger.core.bean;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class GatewaySession extends JsonBean<GatewaySession> {
    private RequestSession requestSession = null;
    private IndicateSession indicateSession = null;

    public RequestSession getRequestSession() {
        return this.requestSession;
    }

    public void setRequestSession(RequestSession requestSession) {
        this.requestSession = requestSession;
    }

    public IndicateSession getIndicateSession() {
        return this.indicateSession;
    }

    public void setIndicateSession(IndicateSession indicateSession) {
        this.indicateSession = indicateSession;
    }
}