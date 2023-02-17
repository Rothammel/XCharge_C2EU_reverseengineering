package com.xcharge.charger.protocol.ocpp.bean.types;

import com.xcharge.common.bean.JsonBean;

public class KeyValue extends JsonBean<KeyValue> {
    private String key;
    private boolean readonly;
    private String value;

    public String getKey() {
        return this.key;
    }

    public void setKey(String key2) {
        this.key = key2;
    }

    public boolean isReadonly() {
        return this.readonly;
    }

    public void setReadonly(boolean readonly2) {
        this.readonly = readonly2;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value2) {
        this.value = value2;
    }
}
