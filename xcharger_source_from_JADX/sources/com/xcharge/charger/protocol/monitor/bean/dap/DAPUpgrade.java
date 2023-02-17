package com.xcharge.charger.protocol.monitor.bean.dap;

import com.xcharge.common.bean.JsonBean;

public class DAPUpgrade extends JsonBean<DAPUpgrade> {
    private String dependent_ver;
    private String pack_md5;
    private long pack_size;
    private String pack_url;
    private String update_ver;

    public String getUpdate_ver() {
        return this.update_ver;
    }

    public void setUpdate_ver(String update_ver2) {
        this.update_ver = update_ver2;
    }

    public String getDependent_ver() {
        return this.dependent_ver;
    }

    public void setDependent_ver(String dependent_ver2) {
        this.dependent_ver = dependent_ver2;
    }

    public String getPack_url() {
        return this.pack_url;
    }

    public void setPack_url(String pack_url2) {
        this.pack_url = pack_url2;
    }

    public long getPack_size() {
        return this.pack_size;
    }

    public void setPack_size(long pack_size2) {
        this.pack_size = pack_size2;
    }

    public String getPack_md5() {
        return this.pack_md5;
    }

    public void setPack_md5(String pack_md52) {
        this.pack_md5 = pack_md52;
    }
}
