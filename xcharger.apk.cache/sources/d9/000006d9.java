package com.xcharge.charger.protocol.family.xcloud.session;

import com.xcharge.charger.data.bean.ErrorCode;
import com.xcharge.charger.protocol.family.xcloud.bean.XCloudMessage;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class XCloudRequestSession extends JsonBean<XCloudRequestSession> {
    private XCloudMessage request = null;
    private XCloudMessage response = null;
    private ErrorCode error = null;

    public XCloudMessage getRequest() {
        return this.request;
    }

    public void setRequest(XCloudMessage request) {
        this.request = request;
    }

    public XCloudMessage getResponse() {
        return this.response;
    }

    public void setResponse(XCloudMessage response) {
        this.response = response;
    }

    public ErrorCode getError() {
        return this.error;
    }

    public void setError(ErrorCode error) {
        this.error = error;
    }
}