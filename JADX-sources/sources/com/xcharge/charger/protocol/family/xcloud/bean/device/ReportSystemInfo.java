package com.xcharge.charger.protocol.family.xcloud.bean.device;

import com.xcharge.charger.protocol.family.xcloud.bean.DeviceCapability;
import com.xcharge.charger.protocol.family.xcloud.bean.DeviceSetting;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class ReportSystemInfo extends JsonBean<ReportSystemInfo> {
    private Long sid = null;
    private String version = null;
    private DeviceSetting setting = null;
    private String iccid = null;
    private String simId = null;
    private long time = 0;
    private DeviceCapability capabilities = null;
    private Double ammeter = null;

    public Long getSid() {
        return this.sid;
    }

    public void setSid(Long sid) {
        this.sid = sid;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public DeviceSetting getSetting() {
        return this.setting;
    }

    public void setSetting(DeviceSetting setting) {
        this.setting = setting;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getIccid() {
        return this.iccid;
    }

    public void setIccid(String iccid) {
        this.iccid = iccid;
    }

    public String getSimId() {
        return this.simId;
    }

    public void setSimId(String simId) {
        this.simId = simId;
    }

    public DeviceCapability getCapabilities() {
        return this.capabilities;
    }

    public void setCapabilities(DeviceCapability capabilities) {
        this.capabilities = capabilities;
    }

    public Double getAmmeter() {
        return this.ammeter;
    }

    public void setAmmeter(Double ammeter) {
        this.ammeter = ammeter;
    }
}
