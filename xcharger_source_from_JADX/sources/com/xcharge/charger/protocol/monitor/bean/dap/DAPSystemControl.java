package com.xcharge.charger.protocol.monitor.bean.dap;

import com.xcharge.common.bean.JsonBean;

public class DAPSystemControl extends JsonBean<DAPSystemControl> {
    public static final String REBOOT = "reboot";
    public static final String RESTORE = "restore";

    /* renamed from: id */
    private String f98id = null;
    private String type = null;
    private String value = null;

    public String getId() {
        return this.f98id;
    }

    public void setId(String id) {
        this.f98id = id;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type2) {
        this.type = type2;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value2) {
        this.value = value2;
    }
}
