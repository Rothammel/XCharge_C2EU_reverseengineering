package com.xcharge.charger.protocol.family.xcloud.bean.device;

import com.xcharge.charger.protocol.family.xcloud.bean.DeviceCapability;
import com.xcharge.charger.protocol.family.xcloud.bean.DeviceSetting;
import com.xcharge.common.bean.JsonBean;

public class ReportSystemInfo extends JsonBean<ReportSystemInfo> {
    private Double ammeter = null;
    private DeviceCapability capabilities = null;
    private String iccid = null;
    private DeviceSetting setting = null;
    private Long sid = null;
    private String simId = null;
    private long time = 0;
    private String version = null;

    public Long getSid() {
        return this.sid;
    }

    public void setSid(Long sid2) {
        this.sid = sid2;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version2) {
        this.version = version2;
    }

    public DeviceSetting getSetting() {
        return this.setting;
    }

    public void setSetting(DeviceSetting setting2) {
        this.setting = setting2;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time2) {
        this.time = time2;
    }

    public String getIccid() {
        return this.iccid;
    }

    public void setIccid(String iccid2) {
        this.iccid = iccid2;
    }

    public String getSimId() {
        return this.simId;
    }

    public void setSimId(String simId2) {
        this.simId = simId2;
    }

    public DeviceCapability getCapabilities() {
        return this.capabilities;
    }

    public void setCapabilities(DeviceCapability capabilities2) {
        this.capabilities = capabilities2;
    }

    public Double getAmmeter() {
        return this.ammeter;
    }

    public void setAmmeter(Double ammeter2) {
        this.ammeter = ammeter2;
    }
}
