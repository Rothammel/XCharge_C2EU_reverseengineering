package com.xcharge.charger.protocol.xmsz.session;

import com.xcharge.charger.protocol.xmsz.bean.XMSZMessage;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class XMSZRequestSession extends JsonBean<XMSZRequestSession> {
    private XMSZMessage sendedRequest = null;
    private XMSZMessage response = null;

    public XMSZMessage getSendedRequest() {
        return this.sendedRequest;
    }

    public void setSendedRequest(XMSZMessage sendedRequest) {
        this.sendedRequest = sendedRequest;
    }

    public XMSZMessage getResponse() {
        return this.response;
    }

    public void setResponse(XMSZMessage response) {
        this.response = response;
    }
}
