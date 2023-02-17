package com.xcharge.charger.protocol.monitor.bean.dap;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class DAPUpgrade extends JsonBean<DAPUpgrade> {
    private String dependent_ver;
    private String pack_md5;
    private long pack_size;
    private String pack_url;
    private String update_ver;

    public String getUpdate_ver() {
        return this.update_ver;
    }

    public void setUpdate_ver(String update_ver) {
        this.update_ver = update_ver;
    }

    public String getDependent_ver() {
        return this.dependent_ver;
    }

    public void setDependent_ver(String dependent_ver) {
        this.dependent_ver = dependent_ver;
    }

    public String getPack_url() {
        return this.pack_url;
    }

    public void setPack_url(String pack_url) {
        this.pack_url = pack_url;
    }

    public long getPack_size() {
        return this.pack_size;
    }

    public void setPack_size(long pack_size) {
        this.pack_size = pack_size;
    }

    public String getPack_md5() {
        return this.pack_md5;
    }

    public void setPack_md5(String pack_md5) {
        this.pack_md5 = pack_md5;
    }
}