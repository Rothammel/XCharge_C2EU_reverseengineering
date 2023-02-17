package com.xcharge.charger.protocol.monitor.bean.dap;

import com.xcharge.common.bean.JsonBean;

public class DAPQrcode extends JsonBean<DAPQrcode> {
    private String content;
    private long expires_in;
    private String url;

    public String getContent() {
        return this.content;
    }

    public void setContent(String content2) {
        this.content = content2;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url2) {
        this.url = url2;
    }

    public long getExpires_in() {
        return this.expires_in;
    }

    public void setExpires_in(long expires_in2) {
        this.expires_in = expires_in2;
    }
}
