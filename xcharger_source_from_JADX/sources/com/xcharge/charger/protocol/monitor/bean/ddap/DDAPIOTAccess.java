package com.xcharge.charger.protocol.monitor.bean.ddap;

import com.xcharge.common.bean.JsonBean;

public class DDAPIOTAccess extends JsonBean<DDAPIOTAccess> {
    private String device_id = null;
    private String device_name = null;
    private String device_secret = null;
    private String host = null;
    private String product_key = null;
    private String product_secret = null;

    public String getProduct_key() {
        return this.product_key;
    }

    public void setProduct_key(String product_key2) {
        this.product_key = product_key2;
    }

    public String getProduct_secret() {
        return this.product_secret;
    }

    public void setProduct_secret(String product_secret2) {
        this.product_secret = product_secret2;
    }

    public String getDevice_id() {
        return this.device_id;
    }

    public void setDevice_id(String device_id2) {
        this.device_id = device_id2;
    }

    public String getDevice_name() {
        return this.device_name;
    }

    public void setDevice_name(String device_name2) {
        this.device_name = device_name2;
    }

    public String getDevice_secret() {
        return this.device_secret;
    }

    public void setDevice_secret(String device_secret2) {
        this.device_secret = device_secret2;
    }

    public String getHost() {
        return this.host;
    }

    public void setHost(String host2) {
        this.host = host2;
    }
}
