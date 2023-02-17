package com.xcharge.charger.device.p005c2.bean;

import com.xcharge.common.bean.JsonBean;

/* renamed from: com.xcharge.charger.device.c2.bean.AuthValue */
public class AuthValue extends JsonBean<AuthValue> {
    private String cause = null;
    private boolean value = false;

    public boolean isValue() {
        return this.value;
    }

    public void setValue(boolean value2) {
        this.value = value2;
    }

    public String getCause() {
        return this.cause;
    }

    public void setCause(String cause2) {
        this.cause = cause2;
    }
}
