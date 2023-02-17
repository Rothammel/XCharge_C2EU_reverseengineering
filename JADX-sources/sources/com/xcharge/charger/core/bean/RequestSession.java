package com.xcharge.charger.core.bean;

import com.xcharge.charger.core.api.bean.DCAPMessage;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class RequestSession extends JsonBean<RequestSession> {
    private DCAPMessage request = null;
    private DCAPMessage confirm = null;

    public DCAPMessage getRequest() {
        return this.request;
    }

    public void setRequest(DCAPMessage request) {
        this.request = request;
    }

    public DCAPMessage getConfirm() {
        return this.confirm;
    }

    public void setConfirm(DCAPMessage confirm) {
        this.confirm = confirm;
    }
}
