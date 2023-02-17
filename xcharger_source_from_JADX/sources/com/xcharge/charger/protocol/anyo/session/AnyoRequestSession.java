package com.xcharge.charger.protocol.anyo.session;

import com.xcharge.charger.protocol.anyo.bean.AnyoMessage;
import com.xcharge.common.bean.JsonBean;

public class AnyoRequestSession extends JsonBean<AnyoRequestSession> {
    private AnyoMessage response = null;
    private AnyoMessage sendedRequest = null;

    public AnyoMessage getSendedRequest() {
        return this.sendedRequest;
    }

    public void setSendedRequest(AnyoMessage sendedRequest2) {
        this.sendedRequest = sendedRequest2;
    }

    public AnyoMessage getResponse() {
        return this.response;
    }

    public void setResponse(AnyoMessage response2) {
        this.response = response2;
    }
}
