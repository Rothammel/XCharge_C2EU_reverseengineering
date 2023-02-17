package com.xcharge.charger.core.bean;

import com.xcharge.charger.core.api.bean.DCAPMessage;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class IndicateSession extends JsonBean<IndicateSession> {
    private DCAPMessage indicate = null;
    private DCAPMessage response = null;

    public DCAPMessage getIndicate() {
        return this.indicate;
    }

    public void setIndicate(DCAPMessage indicate) {
        this.indicate = indicate;
    }

    public DCAPMessage getResponse() {
        return this.response;
    }

    public void setResponse(DCAPMessage response) {
        this.response = response;
    }
}