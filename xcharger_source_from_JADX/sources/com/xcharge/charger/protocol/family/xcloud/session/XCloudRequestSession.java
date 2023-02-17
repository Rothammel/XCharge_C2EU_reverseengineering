package com.xcharge.charger.protocol.family.xcloud.session;

import com.xcharge.charger.data.bean.ErrorCode;
import com.xcharge.charger.protocol.family.xcloud.bean.XCloudMessage;
import com.xcharge.common.bean.JsonBean;

public class XCloudRequestSession extends JsonBean<XCloudRequestSession> {
    private ErrorCode error = null;
    private XCloudMessage request = null;
    private XCloudMessage response = null;

    public XCloudMessage getRequest() {
        return this.request;
    }

    public void setRequest(XCloudMessage request2) {
        this.request = request2;
    }

    public XCloudMessage getResponse() {
        return this.response;
    }

    public void setResponse(XCloudMessage response2) {
        this.response = response2;
    }

    public ErrorCode getError() {
        return this.error;
    }

    public void setError(ErrorCode error2) {
        this.error = error2;
    }
}
