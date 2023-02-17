package com.xcharge.charger.protocol.monitor.bean.dap;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class Advsite extends JsonBean<Advsite> {
    private long duration;
    private String md5;
    private String type;
    private String url;

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMd5() {
        return this.md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public long getDuration() {
        return this.duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}