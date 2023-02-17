package com.xcharge.charger.protocol.monitor.bean.dap;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class DAPVersion extends JsonBean<DAPVersion> {
    private String app_ver;
    private String firmware_ver;
    private String os_ver;

    public String getOs_ver() {
        return this.os_ver;
    }

    public void setOs_ver(String os_ver) {
        this.os_ver = os_ver;
    }

    public String getFirmware_ver() {
        return this.firmware_ver;
    }

    public void setFirmware_ver(String firmware_ver) {
        this.firmware_ver = firmware_ver;
    }

    public String getApp_ver() {
        return this.app_ver;
    }

    public void setApp_ver(String app_ver) {
        this.app_ver = app_ver;
    }
}