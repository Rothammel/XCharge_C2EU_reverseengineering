package com.xcharge.charger.data.bean.setting;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class ChargeFullRequirement extends JsonBean<ChargeFullRequirement> {
    private double amp = 0.0d;
    private double soc = 1.0d;

    public double getAmp() {
        return this.amp;
    }

    public void setAmp(double amp) {
        this.amp = amp;
    }

    public double getSoc() {
        return this.soc;
    }

    public void setSoc(double soc) {
        this.soc = soc;
    }
}