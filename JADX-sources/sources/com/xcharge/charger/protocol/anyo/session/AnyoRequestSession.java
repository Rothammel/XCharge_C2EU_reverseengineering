package com.xcharge.charger.protocol.anyo.session;

import com.xcharge.charger.protocol.anyo.bean.AnyoMessage;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class AnyoRequestSession extends JsonBean<AnyoRequestSession> {
    private AnyoMessage sendedRequest = null;
    private AnyoMessage response = null;

    public AnyoMessage getSendedRequest() {
        return this.sendedRequest;
    }

    public void setSendedRequest(AnyoMessage sendedRequest) {
        this.sendedRequest = sendedRequest;
    }

    public AnyoMessage getResponse() {
        return this.response;
    }

    public void setResponse(AnyoMessage response) {
        this.response = response;
    }
}
