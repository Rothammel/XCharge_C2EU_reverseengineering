package com.xcharge.charger.protocol.monitor.bean.cap;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class CAPAuthInfo extends JsonBean<CAPAuthInfo> {
    private String auth_key = null;

    public String getAuth_key() {
        return this.auth_key;
    }

    public void setAuth_key(String auth_key) {
        this.auth_key = auth_key;
    }
}
