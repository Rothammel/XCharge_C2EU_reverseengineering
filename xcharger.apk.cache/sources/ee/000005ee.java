package com.xcharge.charger.device.c2.bean;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class AuthValue extends JsonBean<AuthValue> {
    private boolean value = false;
    private String cause = null;

    public boolean isValue() {
        return this.value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    public String getCause() {
        return this.cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }
}