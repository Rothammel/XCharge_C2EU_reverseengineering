package com.xcharge.charger.protocol.family.xcloud.bean;

import com.xcharge.common.bean.JsonBean;

public class XCloudPort extends JsonBean<XCloudPort> {
    private Boolean enabled = null;
    private Boolean lockEnabled = null;
    private Integer powerPhase = null;
    private Double powerSupply = null;
    private Integer powerSupplyPercent = null;

    public Boolean isLockEnabled() {
        return this.lockEnabled;
    }

    public void setLockEnabled(Boolean lockEnabled2) {
        this.lockEnabled = lockEnabled2;
    }

    public Boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(Boolean enabled2) {
        this.enabled = enabled2;
    }

    public Integer getPowerPhase() {
        return this.powerPhase;
    }

    public void setPowerPhase(Integer powerPhase2) {
        this.powerPhase = powerPhase2;
    }

    public Integer getPowerSupplyPercent() {
        return this.powerSupplyPercent;
    }

    public void setPowerSupplyPercent(Integer powerSupplyPercent2) {
        this.powerSupplyPercent = powerSupplyPercent2;
    }

    public Double getPowerSupply() {
        return this.powerSupply;
    }

    public void setPowerSupply(Double powerSupply2) {
        this.powerSupply = powerSupply2;
    }
}
