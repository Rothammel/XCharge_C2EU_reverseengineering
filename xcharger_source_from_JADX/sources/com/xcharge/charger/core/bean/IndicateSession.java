package com.xcharge.charger.core.bean;

import com.xcharge.charger.core.api.bean.DCAPMessage;
import com.xcharge.common.bean.JsonBean;

public class IndicateSession extends JsonBean<IndicateSession> {
    private DCAPMessage indicate = null;
    private DCAPMessage response = null;

    public DCAPMessage getIndicate() {
        return this.indicate;
    }

    public void setIndicate(DCAPMessage indicate2) {
        this.indicate = indicate2;
    }

    public DCAPMessage getResponse() {
        return this.response;
    }

    public void setResponse(DCAPMessage response2) {
        this.response = response2;
    }
}
