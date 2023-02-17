package com.xcharge.charger.protocol.ocpp.bean.types;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class KeyValue extends JsonBean<KeyValue> {
    private String key;
    private boolean readonly;
    private String value;

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean isReadonly() {
        return this.readonly;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}