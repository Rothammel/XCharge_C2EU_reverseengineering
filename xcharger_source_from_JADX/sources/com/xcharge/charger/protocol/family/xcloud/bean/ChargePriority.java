package com.xcharge.charger.protocol.family.xcloud.bean;

import com.xcharge.common.bean.JsonBean;

public class ChargePriority extends JsonBean<ChargePriority> {
    private int level = 0;
    private int powerPhase = 0;
    private double powerSupply = 0.0d;
    private int powerSupplyPercent = 0;

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level2) {
        this.level = level2;
    }

    public int getPowerPhase() {
        return this.powerPhase;
    }

    public void setPowerPhase(int powerPhase2) {
        this.powerPhase = powerPhase2;
    }

    public int getPowerSupplyPercent() {
        return this.powerSupplyPercent;
    }

    public void setPowerSupplyPercent(int powerSupplyPercent2) {
        this.powerSupplyPercent = powerSupplyPercent2;
    }

    public double getPowerSupply() {
        return this.powerSupply;
    }

    public void setPowerSupply(double powerSupply2) {
        this.powerSupply = powerSupply2;
    }
}
