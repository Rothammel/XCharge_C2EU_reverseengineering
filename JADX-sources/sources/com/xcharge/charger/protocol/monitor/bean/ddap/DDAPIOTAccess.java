package com.xcharge.charger.protocol.monitor.bean.ddap;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class DDAPIOTAccess extends JsonBean<DDAPIOTAccess> {
    private String product_key = null;
    private String product_secret = null;
    private String device_id = null;
    private String device_name = null;
    private String device_secret = null;
    private String host = null;

    public String getProduct_key() {
        return this.product_key;
    }

    public void setProduct_key(String product_key) {
        this.product_key = product_key;
    }

    public String getProduct_secret() {
        return this.product_secret;
    }

    public void setProduct_secret(String product_secret) {
        this.product_secret = product_secret;
    }

    public String getDevice_id() {
        return this.device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public String getDevice_name() {
        return this.device_name;
    }

    public void setDevice_name(String device_name) {
        this.device_name = device_name;
    }

    public String getDevice_secret() {
        return this.device_secret;
    }

    public void setDevice_secret(String device_secret) {
        this.device_secret = device_secret;
    }

    public String getHost() {
        return this.host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
