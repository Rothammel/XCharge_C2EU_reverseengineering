package com.xcharge.charger.data.bean.setting;

import com.xcharge.common.bean.JsonBean;

public class ConsoleSetting extends JsonBean<ConsoleSetting> {
    public static final String SCHEMA_HTTP = "http";

    /* renamed from: ip */
    private String f58ip = null;
    private int port = 0;
    private String schema = SCHEMA_HTTP;

    public String getSchema() {
        return this.schema;
    }

    public void setSchema(String schema2) {
        this.schema = schema2;
    }

    public String getIp() {
        return this.f58ip;
    }

    public void setIp(String ip) {
        this.f58ip = ip;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port2) {
        this.port = port2;
    }
}
