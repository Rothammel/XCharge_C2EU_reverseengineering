package com.xcharge.charger.core.api.bean.cap;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class StopDirective extends JsonBean<StopDirective> {
    private String device_id = null;
    private String port = null;

    public String getDevice_id() {
        return this.device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public String getPort() {
        return this.port;
    }

    public void setPort(String port) {
        this.port = port;
    }
}