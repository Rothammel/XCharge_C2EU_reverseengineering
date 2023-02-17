package com.xcharge.charger.data.bean.setting;

import com.xcharge.common.bean.JsonBean;

public class ChargeFullRequirement extends JsonBean<ChargeFullRequirement> {
    private double amp = 0.0d;
    private double soc = 1.0d;

    public double getAmp() {
        return this.amp;
    }

    public void setAmp(double amp2) {
        this.amp = amp2;
    }

    public double getSoc() {
        return this.soc;
    }

    public void setSoc(double soc2) {
        this.soc = soc2;
    }
}
