package com.xcharge.charger.core.bean;

import com.xcharge.charger.core.api.bean.DCAPMessage;
import com.xcharge.common.bean.JsonBean;

public class RequestSession extends JsonBean<RequestSession> {
    private DCAPMessage confirm = null;
    private DCAPMessage request = null;

    public DCAPMessage getRequest() {
        return this.request;
    }

    public void setRequest(DCAPMessage request2) {
        this.request = request2;
    }

    public DCAPMessage getConfirm() {
        return this.confirm;
    }

    public void setConfirm(DCAPMessage confirm2) {
        this.confirm = confirm2;
    }
}
