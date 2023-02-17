package com.xcharge.charger.protocol.monitor.bean.dap;

import com.xcharge.common.bean.JsonBean;

public class Advsite extends JsonBean<Advsite> {
    private long duration;
    private String md5;
    private String type;
    private String url;

    public String getType() {
        return this.type;
    }

    public void setType(String type2) {
        this.type = type2;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url2) {
        this.url = url2;
    }

    public String getMd5() {
        return this.md5;
    }

    public void setMd5(String md52) {
        this.md5 = md52;
    }

    public long getDuration() {
        return this.duration;
    }

    public void setDuration(long duration2) {
        this.duration = duration2;
    }
}
