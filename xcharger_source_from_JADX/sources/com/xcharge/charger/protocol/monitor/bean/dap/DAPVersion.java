package com.xcharge.charger.protocol.monitor.bean.dap;

import com.xcharge.common.bean.JsonBean;

public class DAPVersion extends JsonBean<DAPVersion> {
    private String app_ver;
    private String firmware_ver;
    private String os_ver;

    public String getOs_ver() {
        return this.os_ver;
    }

    public void setOs_ver(String os_ver2) {
        this.os_ver = os_ver2;
    }

    public String getFirmware_ver() {
        return this.firmware_ver;
    }

    public void setFirmware_ver(String firmware_ver2) {
        this.firmware_ver = firmware_ver2;
    }

    public String getApp_ver() {
        return this.app_ver;
    }

    public void setApp_ver(String app_ver2) {
        this.app_ver = app_ver2;
    }
}
