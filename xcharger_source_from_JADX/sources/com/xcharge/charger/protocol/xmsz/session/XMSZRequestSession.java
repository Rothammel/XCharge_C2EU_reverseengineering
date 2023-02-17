package com.xcharge.charger.protocol.xmsz.session;

import com.xcharge.charger.protocol.xmsz.bean.XMSZMessage;
import com.xcharge.common.bean.JsonBean;

public class XMSZRequestSession extends JsonBean<XMSZRequestSession> {
    private XMSZMessage response = null;
    private XMSZMessage sendedRequest = null;

    public XMSZMessage getSendedRequest() {
        return this.sendedRequest;
    }

    public void setSendedRequest(XMSZMessage sendedRequest2) {
        this.sendedRequest = sendedRequest2;
    }

    public XMSZMessage getResponse() {
        return this.response;
    }

    public void setResponse(XMSZMessage response2) {
        this.response = response2;
    }
}
