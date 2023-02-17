package com.xcharge.charger.protocol.family.xcloud.bean;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class XCloudPort extends JsonBean<XCloudPort> {
    private Boolean enabled = null;
    private Boolean lockEnabled = null;
    private Integer powerPhase = null;
    private Integer powerSupplyPercent = null;
    private Double powerSupply = null;

    public Boolean isLockEnabled() {
        return this.lockEnabled;
    }

    public void setLockEnabled(Boolean lockEnabled) {
        this.lockEnabled = lockEnabled;
    }

    public Boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getPowerPhase() {
        return this.powerPhase;
    }

    public void setPowerPhase(Integer powerPhase) {
        this.powerPhase = powerPhase;
    }

    public Integer getPowerSupplyPercent() {
        return this.powerSupplyPercent;
    }

    public void setPowerSupplyPercent(Integer powerSupplyPercent) {
        this.powerSupplyPercent = powerSupplyPercent;
    }

    public Double getPowerSupply() {
        return this.powerSupply;
    }

    public void setPowerSupply(Double powerSupply) {
        this.powerSupply = powerSupply;
    }
}
