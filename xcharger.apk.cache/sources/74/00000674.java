package com.xcharge.charger.protocol.family.xcloud.bean;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class ChargePriority extends JsonBean<ChargePriority> {
    private int level = 0;
    private int powerPhase = 0;
    private int powerSupplyPercent = 0;
    private double powerSupply = 0.0d;

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getPowerPhase() {
        return this.powerPhase;
    }

    public void setPowerPhase(int powerPhase) {
        this.powerPhase = powerPhase;
    }

    public int getPowerSupplyPercent() {
        return this.powerSupplyPercent;
    }

    public void setPowerSupplyPercent(int powerSupplyPercent) {
        this.powerSupplyPercent = powerSupplyPercent;
    }

    public double getPowerSupply() {
        return this.powerSupply;
    }

    public void setPowerSupply(double powerSupply) {
        this.powerSupply = powerSupply;
    }
}